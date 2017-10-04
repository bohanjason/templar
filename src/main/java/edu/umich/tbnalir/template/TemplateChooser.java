package edu.umich.tbnalir.template;

import com.esotericsoftware.minlog.Log;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import edu.umich.tbnalir.components.NodeMapper;
import edu.umich.tbnalir.components.StanfordNLParser;
import edu.umich.tbnalir.dataStructure.ParseTreeNode;
import edu.umich.tbnalir.dataStructure.Query;
import edu.umich.tbnalir.parse.*;
import edu.umich.tbnalir.rdbms.*;
import edu.umich.tbnalir.sql.Operator;
import edu.umich.tbnalir.tools.PrintForCheck;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by cjbaik on 9/6/17.
 */
public class TemplateChooser {
    Map<String, Relation> relations;

    public TemplateChooser(Map<String, Relation> relations) {
        this.relations = relations;
    }

    public List<PossibleTranslation> generatePossibleTranslationsRecursive(List<ParseTreeNode> remainingNodes,
                                                                           Set<Relation> accumRel,
                                                                           List<Projection> accumProj,
                                                                           List<Predicate> accumPred,
                                                                           List<Having> accumHaving,
                                                                           Superlative superlative,
                                                                           double accumScore, double accumNodes) {
        List<PossibleTranslation> result = new ArrayList<>();

        if (accumRel == null) accumRel = new HashSet<>();
        if (accumProj == null) accumProj = new ArrayList<>();
        if (accumPred == null) accumPred = new ArrayList<>();
        if (accumHaving == null) accumHaving = new ArrayList<>();

        // Base case: generate current possible translations now
        if (remainingNodes.size() == 0) {
            // If projections are empty, generate from primary attributes of relations in translation
            if (accumProj.isEmpty()) {
                /*
                for (Relation rel : accumRel) {
                    if (rel.getPrimaryAttr() != null) {
                        List<Projection> newAccumProj = new ArrayList<>(accumProj);
                        newAccumProj.add(new Projection(rel.getPrimaryAttr(), null));

                        // TODO: Also arbitrary 0.8 added here!
                        result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                new HashSet<>(accumRel), newAccumProj, new ArrayList<>(accumPred),
                                new ArrayList<>(accumHaving), superlative, accumScore + 0.8, accumNodes + 1));
                    }
                }*/
            } else {
                PossibleTranslation translation = new PossibleTranslation();
                translation.setRelations(new HashSet<>(accumRel));
                translation.setProjections(new ArrayList<>(accumProj));
                translation.setPredicates(new ArrayList<>(accumPred));
                translation.setHavings(new ArrayList<>(accumHaving));
                translation.setSuperlative(superlative);
                translation.setTranslationScore(accumScore / accumNodes);
                result.add(translation);
            }
            return result;
        }

        ParseTreeNode curNode = remainingNodes.remove(0);

        if (curNode.tokenType.equals("NT") || curNode.tokenType.startsWith("VT")) {
            curNode.mappedElements.sort((a, b) -> Double.valueOf(b.similarity).compareTo(a.similarity));

            for (MappedSchemaElement schemaEl : curNode.mappedElements.subList(0, Math.min(5, curNode.mappedElements.size()))) {
                // Min threshold to even try...
                if (schemaEl.similarity < 0.5) {
                    continue;
                }

                Relation rel = this.relations.get(schemaEl.schemaElement.relation.name);
                if (rel == null)
                    throw new RuntimeException("Relation " + schemaEl.schemaElement.relation.name + " not found.");

                // If we're dealing with a relation, generate a version without it, and also move ahead
                if (schemaEl.schemaElement.type.equals("relation")) {
                    // TODO: arbitrary 0.8 added here - what to do?
                    result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                            new HashSet<>(accumRel), new ArrayList<>(accumProj), new ArrayList<>(accumPred),
                            new ArrayList<>(accumHaving), superlative, accumScore + 0.8, accumNodes + 1));

                    Set<Relation> newAccumRel = new HashSet<>(accumRel);
                    newAccumRel.add(rel);

                    result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                            newAccumRel, new ArrayList<>(accumProj), new ArrayList<>(accumPred),
                            new ArrayList<>(accumHaving), superlative, accumScore + schemaEl.similarity, accumNodes + 1));
                    continue;
                }

                Attribute attr = rel.getAttributes().get(schemaEl.schemaElement.name);
                if (attr == null)
                    throw new RuntimeException("Attribute " + schemaEl.schemaElement.name + " not found.");

                // Copy relevant structures so recursive operations don't interfere with it
                Set<Relation> newAccumRel = new HashSet<>(accumRel);
                List<Projection> newAccumProj = new ArrayList<>(accumProj);
                List<Predicate> newAccumPred = new ArrayList<>(accumPred);
                List<Having> newAccumHaving = new ArrayList<>(accumHaving);

                // Treat as projection, if no mapped values or if parent is CMT
                if (schemaEl.mappedValues.isEmpty() || schemaEl.choice == -1) {
                    // Treat as superlative if has both superlative and function
                    if (curNode.attachedSuperlative != null ||
                            (schemaEl.attachedFT != null &&
                                    (schemaEl.attachedFT.equals("max") || schemaEl.attachedFT.equals("min")))) {
                        String funcStr;
                        String superlativeStr;
                        if (curNode.attachedSuperlative != null) {
                            superlativeStr = curNode.attachedSuperlative;
                            funcStr = schemaEl.attachedFT;
                        } else {
                            superlativeStr = schemaEl.attachedFT;
                            funcStr = null;
                        }
                        boolean desc = superlativeStr.equals("max");

                        Superlative newSuper = new Superlative(attr, funcStr, desc);
                        if (!newAccumRel.contains(attr.getRelation())) {
                            newAccumRel.add(attr.getRelation());
                        }

                        result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                newAccumRel, newAccumProj, newAccumPred, newAccumHaving, newSuper,
                                accumScore + schemaEl.similarity, accumNodes + 1));
                    } else {
                        Projection proj = new Projection(attr, schemaEl.attachedFT);

                        // If you are creating a projection and there already exists a predicate with the same attribute,
                        // then create an additional path without this projection.
                        boolean selfJoinFlag = false;
                        int aliasInt = 0;
                        for (Predicate pred : accumPred) {
                            if (pred.getAttribute().hasSameRelationNameAndNameAs(attr)) {
                                if (!selfJoinFlag) {
                                    result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                            new HashSet<>(accumRel), new ArrayList<>(accumProj), new ArrayList<>(accumPred),
                                            new ArrayList<>(accumHaving), superlative, accumScore + 0.5, accumNodes + 1));
                                }
                                selfJoinFlag = true;
                                aliasInt++;
                            }
                        }

                        // If we have a quantifier, set GROUP BY
                        if (curNode.QT != null && curNode.QT.equals("each")) {
                            proj.setGroupBy(true);
                        }

                        // Increment aliasInt for every shared projection
                        boolean aggregateNewProj = false;
                        for (Projection curProj : accumProj) {
                            if (curProj.getAttribute().hasSameRelationNameAndNameAs(proj.getAttribute())) {
                                aliasInt++;
                            }
                            // If this projection is GROUP BY, aggregate other projections
                            if (proj.isGroupBy()) {
                                curProj.applyAggregateFunction();
                            }

                            // If other projection contains GROUP BY, set aggregate flag to aggregate this one
                            if (curProj.isGroupBy()) {
                                aggregateNewProj = true;
                            }
                        }

                        rel = new Relation(rel);
                        rel.setAliasInt(aliasInt);
                        Attribute newAttr = new Attribute(proj.getAttribute());
                        newAttr.setRelation(rel);
                        proj.setAttribute(newAttr);

                        if (aggregateNewProj) proj.applyAggregateFunction();

                        newAccumProj.add(proj);
                        newAccumRel.add(rel);

                        // Penalize the similarity if this projection is not the child of a CMT (Command Token) node
                        // but only if it's not a group by token
                        boolean likelyProjection = curNode.parent.tokenType.equals("CMT") || proj.isGroupBy();
                        double similarityAdded = schemaEl.similarity;
                        if (!likelyProjection) {
                            similarityAdded *= 0.8;
                        }

                        result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                newAccumRel, newAccumProj, newAccumPred, newAccumHaving, superlative,
                                accumScore + similarityAdded, accumNodes + 1));
                    }
                } else {
                    // For predicates and havings.
                    Operator op = null;
                    String value = null;

                    // Try to find nearby node with operator token if number
                    if (curNode.tokenType.equals("VTNUM")) {
                        if (curNode.attachedOT != null) {
                            switch (curNode.attachedOT) {
                                case ">":
                                    op = Operator.GT;
                                    value = curNode.label;
                                    break;
                                case ">=":
                                    op = Operator.GTE;
                                    value = curNode.label;
                                    break;
                                case "<":
                                    op = Operator.LT;
                                    value = curNode.label;
                                    break;
                                case "<=":
                                    op = Operator.LTE;
                                    value = curNode.label;
                                    break;
                                case "!=":
                                    op = Operator.NE;
                                    value = curNode.label;
                                    break;
                                case "=":
                                    op = Operator.EQ;
                                    break;
                            }
                        }
                    }

                    if (op == null) op = Operator.EQ;

                    if (schemaEl.choice != -1) {
                        // TODO: open up the possibility that you have more than 1 mapped value?
                        if (value == null) {
                            value = schemaEl.mappedValues.get(0);
                        }
                    }

                    // If there is an attached function, this is probably a HAVING, not a predicate.
                    // That is, unless we are a child of a CMT
                    if (schemaEl.attachedFT != null && !curNode.parent.tokenType.equals("CMT")) {
                        // TODO: do we need aliasInt for having?
                        Having having = new Having(attr, op, value, schemaEl.attachedFT);

                        // Should not do the same attribute for having as a projection
                        boolean projExists = false;
                        for (Projection proj : accumProj) {
                            if (having.getAttribute().hasSameRelationNameAndNameAs(proj.getAttribute())) {
                                projExists = true;
                            }
                        }
                        if (projExists) continue;

                        if (!newAccumHaving.contains(having)) newAccumHaving.add(having);
                        newAccumRel.add(rel);

                        result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                newAccumRel, newAccumProj, newAccumPred, newAccumHaving, superlative,
                                accumScore + schemaEl.similarity, accumNodes + 1));
                    } else {
                        // If you are creating a predicate and there already exists a projection with the same attribute,
                        // then create an additional path of eliminating this predicate.
                        int aliasInt = 0;

                        for (Projection p : accumProj) {
                            if (p.getAttribute().hasSameRelationNameAndNameAs(attr)) {
                                result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                        new HashSet<>(accumRel), new ArrayList<>(accumProj), new ArrayList<>(accumPred),
                                        new ArrayList<>(accumHaving), superlative, accumScore + 0.5, accumNodes + 1));

                                aliasInt++;
                            }
                        }

                        Predicate pred = new Predicate(attr, op, value);

                        // Check previous predicates, if same attr exists, increment aliasInt
                        boolean notNumberWithNonEquality = !(curNode.tokenType.equals("VTNUM") && curNode.attachedOT != null
                                && !curNode.attachedOT.equals("="));
                        if (notNumberWithNonEquality) {
                            for (Predicate curPred : accumPred) {
                                if (curPred.getAttribute().hasSameRelationNameAndNameAs(pred.getAttribute())) {
                                    aliasInt++;
                                }
                            }
                        }

                        rel = new Relation(rel);
                        rel.setAliasInt(aliasInt);
                        Attribute newAttr = new Attribute(pred.getAttribute());
                        newAttr.setRelation(rel);
                        pred.setAttribute(newAttr);

                        if (!newAccumPred.contains(pred)) newAccumPred.add(pred);
                        newAccumRel.add(rel);

                        // If it's a pred for a weak entity, then generate a projection for the primary entity
                        if (rel.isWeak() && curNode.parent.tokenType.equals("CMT") &&
                                (curNode.relationship.equals("dobj") || curNode.relationship.equals("nsubj") || curNode.relationship.equals("nsubjpass"))) {
                            Relation parent = this.relations.get(rel.getParent());
                            // TODO: do I need to increment aliasInt here for the parent? how?
                            Projection proj = new Projection(parent.getPrimaryAttr(), curNode.getChoiceMap().attachedFT);
                            newAccumProj.add(proj);
                            newAccumRel.add(parent);
                        }


                        result.addAll(this.generatePossibleTranslationsRecursive(new ArrayList<>(remainingNodes),
                                newAccumRel, newAccumProj, newAccumPred, newAccumHaving, superlative,
                                accumScore + schemaEl.similarity, accumNodes + 1));
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: TemplateChooser <db> <schema-prefix> <join-level> <nlq-file>");
            System.out.println("Example: TemplateChooser mas data/mas/mas 6 data/mas/mas_c1.txt");
            System.exit(1);
        }
        String dbName = args[0];
        String prefix = args[1];
        int joinLevel = Integer.valueOf(args[2]);
        String nlqFile = args[3];

        /*File gModel = new File("libs/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        System.out.println(vec.similarity("cat", "dog"));
        System.out.println(vec.similarity("star", "rating"));
        System.exit(1);*/

        RDBMS db;
        try {
            db = new RDBMS(dbName, prefix);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        SchemaDataTemplateGenerator tg = new SchemaDataTemplateGenerator(db, joinLevel);
        Set<Template> templates = tg.generate();

        // templates.stream().map(Template::toString).forEach(System.out::println);

        // Read in Stanford Parser Model
        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        // Read in stop words list
        List<String> stopwords = new ArrayList<>();
        List<String> queryStrs = new ArrayList<>();
        try {
            List<String> stopwordsList = FileUtils.readLines(new File("libs/stopwords.txt"), "UTF-8");
            for (String word : stopwordsList) {
                stopwords.add(word.trim());
            }

            //queryStrs.addAll(FileUtils.readLines(new File(nlqFile), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
           throw new RuntimeException(e);
        }

        queryStrs.add("which business has the most number of checkins");
        queryStrs.add("find the user with the most number of reviews");
        queryStrs.add("Find the business with the most number of reviews in April");
        queryStrs.add("Find the business which has the most number of categories");

        int i = 0;
        for (String queryStr : queryStrs) {
            Log.info("================");
            Log.info("QUERY " + i++ + ": " + queryStr);
            Log.info("================");

            // TODO: hack, to convert TX to Texas, probably don't need with something like word2vec
            queryStr = queryStr.replace("TX", "Texas");
            queryStr = queryStr.replace("PA", "Pennsylvania");

            Query query = new Query(queryStr, db.schemaGraph);

            Log.info("Parsing query with NL parser...");
            StanfordNLParser.parse(query, lexiParser);

            // Check stopwords list and remove
            List<ParseTreeNode> nodesToRemove = new ArrayList<>();
            for (ParseTreeNode node : query.parseTree.allNodes) {
                if (stopwords.contains(node.label.toLowerCase())) {
                    nodesToRemove.add(node);
                }
            }
            for (ParseTreeNode node : nodesToRemove) {
                query.parseTree.deleteNode(node);
            }

            List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(query.sentence.outputWords); // use Stanford parser to parse a sentence;
            Tree parse = lexiParser.apply(rawWords);
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            List<TypedDependency> dependencyList = gs.typedDependencies(false);

            for (TaggedWord tagged : parse.taggedYield()) {
                System.out.println(tagged.word() + ", " + tagged.tag());
            }

            for (TypedDependency dep : dependencyList) {
                System.out.println(dep);
            }

            Log.info("Mapping nodes to token types...");
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document tokens = builder.parse(new File("libs/tokens.xml"));
                NodeMapper.phraseProcess(query, db, tokens);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            System.out.println("PARSE TREE:");
            System.out.println(query.parseTree);
            System.out.println();

            List<ParseTreeNode> mappedNodes = new ArrayList<>();
            List<ParseTreeNode> mappedNodesToRemove = new ArrayList<>();

            for (ParseTreeNode node : query.parseTree.allNodes) {
                boolean isNameToken = node.tokenType.equals("NT");
                boolean isValueToken = node.tokenType.startsWith("VT");

                // TODO: move all this logic to Fei's components or clean it up
                if (isNameToken || isValueToken) {
                    mappedNodes.add(node);

                    // Check for related nodes that are auxiliary and delete
                    for (ParseTreeNode[] auxEntry : query.auxTable) {
                        // If governing node
                        if (auxEntry[1].equals(node)) {
                            mappedNodesToRemove.add(auxEntry[0]);
                        }
                    }

                    // In the case that we have a function as a parent, add accordingly and "ignore" function
                    if (!node.parent.function.equals("NA")) {
                        String superlative = null;
                        for (MappedSchemaElement mse : node.mappedElements) {
                            mse.attachedFT = node.parent.function;
                        }
                        ParseTreeNode functionNode = node.parent;

                        if (functionNode.parent.function.equals("max") || functionNode.parent.function.equals("min")) {
                            superlative = node.parent.parent.function;
                        }

                        // Only move around children if the function isn't actually a CMT (like "how many")
                        if (!functionNode.tokenType.equals("CMT")) {
                            for (ParseTreeNode funcChild : functionNode.children) {
                                funcChild.parent = node;
                                node.children.add(funcChild);

                                if (funcChild.function.equals("max") || funcChild.function.equals("min")) {
                                    superlative = funcChild.function;
                                }
                            }

                            if (superlative != null) {
                                node.attachedSuperlative = superlative;
                            }

                            node.parent = functionNode.parent;
                            node.relationship = functionNode.relationship;
                            functionNode.parent.children.remove(functionNode);
                        }
                    } else {
                        // Do similar operation if function is child
                        List<ParseTreeNode> funcToRemove = new ArrayList<>();
                        List<ParseTreeNode> childrenToAdd = new ArrayList<>();
                        for (ParseTreeNode child : node.children) {
                            if (child.tokenType.equals("FT")) {
                                if (child.function.equals("max") || child.function.equals("min")) {
                                    node.attachedSuperlative = child.function;
                                } else {
                                    for (MappedSchemaElement mse : node.mappedElements) {
                                        mse.attachedFT = child.function;
                                    }
                                }
                                childrenToAdd.addAll(child.children);
                                funcToRemove.add(child);
                            }
                        }
                        node.children.removeAll(funcToRemove);
                        node.children.addAll(childrenToAdd);
                    }
                }

                // In the case we have a VT related to an NT, and they share an "amod" (adjective modifier)
                // or "num" (number modifier) or "nn" (noun compound modifier) relationship, merge the two.
                if (isValueToken) {
                    for (ParseTreeNode[] adjEntry : query.adjTable) {
                        ParseTreeNode relatedNode;
                        if (adjEntry[0].equals(node)) {
                            relatedNode = adjEntry[1];
                        } /*else if (adjEntry[1].equals(node)) {
                            relatedNode = adjEntry[0];
                        } */else {
                            continue;
                        }

                        // Only do NTs for the remainder
                        if (!relatedNode.tokenType.equals("NT")) continue;

                        // Leave children of CMTs alone, because they're unlikely to be modified
                        if (relatedNode.parent.tokenType.equals("CMT")) continue;

                        MappedSchemaElement chosenMappedSchemaEl = null;
                        int choice = node.choice;
                        // double nodeSimilarity = node.getChoiceMap().similarity;
                        // double maxSimilarity = node.getChoiceMap().similarity;
                        double maxSimilarity = 0;
                        List<Integer> matchedNodes = new ArrayList<>();
                        String attachedFT = null;

                        boolean matchedNodeEl = false;
                        boolean addNewForPrimaryAttribute = false;

                        for (int j = 0; j < relatedNode.mappedElements.size(); j++) {
                            MappedSchemaElement relatedMappedEl = relatedNode.mappedElements.get(j);
                            SchemaElement relatedEl = relatedMappedEl.schemaElement;

                            for (int k = 0; k < node.mappedElements.size(); k++) {
                                MappedSchemaElement nodeMappedEl = node.mappedElements.get(k);

                                boolean relatedIsRelation = relatedMappedEl.schemaElement.type.equals("relation");
                                boolean relationMatchesAndThisIsPrimary = relatedIsRelation &&
                                        nodeMappedEl.schemaElement.relation.name.equals(relatedMappedEl.schemaElement.relation.name)
                                        && nodeMappedEl.schemaElement.relation.defaultAttribute.equals(nodeMappedEl.schemaElement);
                                boolean attributeMatchesIfBothAttributes = !relatedIsRelation &&
                                        nodeMappedEl.schemaElement.relation.name.equals(relatedMappedEl.schemaElement.relation.name) &&
                                        nodeMappedEl.schemaElement.name.equals(relatedMappedEl.schemaElement.name);
                                if (relationMatchesAndThisIsPrimary
                                        || attributeMatchesIfBothAttributes) {
                                    matchedNodeEl = true;
                                    matchedNodes.add(k);

                                    // Somewhat arbitrarily combine their similarity by averaging and giving a boost
                                    // double combinedScore = (nodeSimilarity + relatedMappedEl.similarity) / 2;

                                    double relatedScore = relatedMappedEl.similarity;
                                    double nodeScore = nodeMappedEl.similarity;

                                    if (nodeScore > 0.6 && relatedScore > 0.6) {
                                        // Weigh each disproportionately and compare
                                        double firstScore = 0.8 * relatedScore + 0.2 * nodeScore;
                                        double secondScore = 0.8 * nodeScore + 0.2 * relatedScore;

                                        double combinedScore = Math.max(firstScore, secondScore);
                                        nodeMappedEl.similarity = combinedScore;

                                        if (combinedScore > maxSimilarity) {
                                            chosenMappedSchemaEl = nodeMappedEl;
                                            maxSimilarity = combinedScore;
                                            choice = k;
                                            addNewForPrimaryAttribute = false;
                                            attachedFT = relatedMappedEl.attachedFT;
                                        }
                                    } else if (nodeScore > 0.6) {
                                        // Add 0.12 to be minimum related score, 0.6, multiplied by weight 0.2
                                        double nodeSim = nodeScore * 0.8 + 0.12;
                                        if (nodeSim > maxSimilarity) {
                                            // In the case that the node is fine as is
                                            chosenMappedSchemaEl = null;
                                            maxSimilarity = nodeSim;
                                            choice = k;
                                            addNewForPrimaryAttribute = false;
                                            attachedFT = null;
                                        }
                                    } else if (relatedScore > 0.6) {
                                        double relatedSim = relatedScore * 0.8 + 0.12;
                                        if (relatedSim > maxSimilarity) {
                                            // In the case that the related is fine as is
                                            chosenMappedSchemaEl = null;
                                            maxSimilarity = relatedSim;
                                            choice = k;
                                            addNewForPrimaryAttribute = false;
                                            attachedFT = null;
                                        }
                                    }
                                }
                            }

                            // In the case that the NT is the primary attribute, perhaps we're doing a COUNT?
                            if (!matchedNodeEl &&
                                    relatedEl.equals(relatedEl.relation.defaultAttribute) && relatedMappedEl.choice == -1) {
                                // Use only the related element's similarity
                                double relatedScore = relatedMappedEl.similarity;

                                if (relatedScore > maxSimilarity) {
                                    chosenMappedSchemaEl = relatedMappedEl;
                                    maxSimilarity = relatedScore;
                                    addNewForPrimaryAttribute = true;
                                }
                            }
                        }

                        // Penalize all unmatched node elements
                        for (int m = 0; m < node.mappedElements.size(); m++) {
                            if (!matchedNodes.contains(m)) {
                                node.mappedElements.get(m).similarity *= 0.8;
                            }
                        }

                        if (chosenMappedSchemaEl != null) {
                            if (addNewForPrimaryAttribute) {
                                chosenMappedSchemaEl.mappedValues.add(node.label);
                                chosenMappedSchemaEl.choice = chosenMappedSchemaEl.mappedValues.size() - 1;

                                node.mappedElements.add(chosenMappedSchemaEl);
                                node.choice = node.mappedElements.size() - 1;
                                chosenMappedSchemaEl.attachedFT = "count";
                            } else {
                                chosenMappedSchemaEl.similarity = maxSimilarity;
                                node.choice = choice;
                                if (attachedFT != null && node.getChoiceMap().attachedFT == null) {
                                    node.getChoiceMap().attachedFT = attachedFT;
                                }
                            }
                            mappedNodesToRemove.add(relatedNode);
                        }
                    }
                }
            }
            mappedNodes.removeAll(mappedNodesToRemove);

            PrintForCheck.allParseTreeNodePrintForCheck(query.parseTree);
            System.out.println();

            TemplateChooser tc = new TemplateChooser(db.schemaGraph.relations);
            List<PossibleTranslation> translations = tc.generatePossibleTranslationsRecursive(mappedNodes, null, null,
                    null, null, null, 0, 0);

            int n = 10;

            Log.info("============");
            Log.info("TRANSLATIONS");
            Log.info("============");
            translations.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));

            List<PossibleTranslation> topNTranslations = translations.subList(0, Math.min(translations.size(), n));
            topNTranslations.stream().map(PossibleTranslation::toStringDebug).forEach(System.out::println);

            List<InstantiatedTemplate> results = new ArrayList<>();
            Map<String, Integer> resultIndexMap = new HashMap<>();

            for (Template tmpl : templates) {
                for (PossibleTranslation trans : topNTranslations) {
                    Set<PossibleTranslation> perms = trans.getAliasPermutations();
                    for (PossibleTranslation perm : perms) {
                        InstantiatedTemplate inst = tmpl.instantiate(perm);
                        if (inst == null) continue;

                        Integer existingIndex = resultIndexMap.get(inst.getValue());

                        if (existingIndex != null) {
                            InstantiatedTemplate existingTmpl = results.get(existingIndex);
                            if (inst.getTotalScore() > existingTmpl.getTotalScore()) {
                                results.set(existingIndex, inst);
                            }
                        } else {
                            resultIndexMap.put(inst.getValue(), results.size());
                            results.add(inst);
                        }
                    }
                }
            }

            results.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));

            System.out.println();
            Log.info("============");
            Log.info("FINAL RESULTS");
            Log.info("============");
            results.subList(0, Math.min(10, results.size())).forEach(System.out::println);

            System.out.println();
        }

    }
}
