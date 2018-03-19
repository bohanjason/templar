package edu.umich.templar.baseline;

import com.opencsv.CSVReader;
import edu.umich.templar.db.*;
import edu.umich.templar.util.Similarity;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.sql.ResultSet;
import java.util.*;

public class SQLizer {
    // If we assume that SQLizer knows the fragment type appropriate for each keyword always
    private boolean typeOracle;

    // Activate the join penalty indiscriminately
    private boolean joinScore;

    private Database db;
    private Similarity sim;

    private Map<Integer, QueryTask> queryTasks = new HashMap<>();

    public SQLizer(Database database, String filename, boolean typeOracle, boolean joinScore) {
        this.typeOracle = typeOracle;
        this.joinScore = joinScore;
        this.db = database;

        try {
            CSVReader reader = new CSVReader(new FileReader(filename));
            this.sim = new Similarity(10000);

            this.readQueryTasks(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readQueryTasks(CSVReader reader) {
        try {
            // Skip first header line
            reader.readNext();

            this.queryTasks = new HashMap<>();

            // Read all fragmentTasks in from CSV file
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                int queryId = Integer.valueOf(nextLine[0]);
                String phrase = nextLine[1];
                String op = nextLine[2];
                String type = nextLine[3];
                String answer = nextLine[7];

                QueryTask task = this.queryTasks.get(queryId);
                if (task == null) {
                    task = new QueryTask(queryId);
                    this.queryTasks.put(queryId, task);
                }

                task.addMapping(new FragmentTask(phrase, op, type, answer));
            }
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<DBElement> getTextCandidateMatches(List<String> tokens, String fragType) {
        Set<DBElement> cands = new HashSet<>();

        // Add relations to candidates always
        cands.addAll(this.db.getAllRelations());

        // Only find attributes if we don't know what type, or it's not a relation
        boolean findAttributes = !this.typeOracle || !fragType.equalsIgnoreCase("r");
        if (findAttributes) cands.addAll(this.db.getAllAttributes());

        // Skip tokens if we find an exact match
        boolean foundExactMatch = false;
        for (DBElement el : cands) {
            if (el instanceof Relation) {
                Relation rel = (Relation) el;
                double sim = this.sim.sim(rel.getName(), String.join(" ", tokens));
                if (sim == 1.0) {
                    foundExactMatch = true;
                    break;
                }
            } else if (el instanceof Attribute) {
                Attribute attr = (Attribute) el;
                double sim = this.sim.sim(attr.getName(), String.join(" ", tokens));
                if (sim == 1.0) {
                    foundExactMatch = true;
                }
            } else {
                throw new RuntimeException("Unexpected DBElement type.");
            }
        }

        // If we are operating in type-oracle mode and fragment is selection, then add similar values as tokens
        // (otherwise, we know it's a projection/relation so we don't need to find values)
        boolean findSimValues = !this.typeOracle || fragType.equalsIgnoreCase("s");

        if (!foundExactMatch && findSimValues) {
            cands.addAll(this.db.getSimilarValues(tokens));
        }

        System.out.println("Cands for '" + String.join(" ", tokens) + "': " + cands.size());
        return cands;
    }

    private List<MatchedDBElement> matchTextCandidates(List<String> tokens, Set<DBElement> textCands, String fragType) {
        List<MatchedDBElement> matchedEls = new ArrayList<>();

        for (DBElement cand : textCands) {
            if (cand instanceof Relation) {
                Relation rel = (Relation) cand;
                double sim = this.sim.sim(rel.getName(), String.join(" ", tokens));

                // If we have a type oracle activated and we know it's a projection, treat as such
                if (this.typeOracle && fragType.equalsIgnoreCase("p")) {
                    matchedEls.add(new MatchedDBElement(rel.getMainAttribute(), sim));
                } else {
                    matchedEls.add(new MatchedDBElement(rel, sim));
                }
            } else if (cand instanceof Attribute) {
                Attribute attr = (Attribute) cand;
                double sim = this.sim.sim(attr.getCleanedName(), String.join(" ", tokens));
                matchedEls.add(new MatchedDBElement(attr, sim));
            } else if (cand instanceof Value) {
                Value val = (Value) cand;

                // For cases like "VLDB conference" where the rel/attr name is embedded in the phrase
                List<String> checkTokens = new ArrayList<>();
                for (String token : tokens) {
                    boolean tokenIsAttrOrRel = token.equalsIgnoreCase(val.getAttribute().getCleanedName()) ||
                            token.equalsIgnoreCase(val.getAttribute().getRelation().getName());
                    if (!tokenIsAttrOrRel) {
                        checkTokens.add(token);
                    }
                }

                double sim = this.sim.sim(val.getValue(), String.join(" ", checkTokens));
                matchedEls.add(new MatchedDBElement(val, sim));
            } else {
                throw new RuntimeException("Invalid DBElement type.");
            }
        }
        return matchedEls;
    }

    private Set<DBElement> getNumericCandidateMatches(String numericToken, String op, String fragType) {
        if (op.isEmpty()) op = "=";

        // If tokens, narrow it down first
        Set<DBElement> cands = new HashSet<>();

        // Add relations to candidates always
        cands.addAll(this.db.getAllRelations());

        // Only find attributes if we don't know what type, or it's not a relation
        boolean findAttributes = !this.typeOracle || !fragType.equalsIgnoreCase("r");
        if (findAttributes) cands.addAll(this.db.getNumericAttributes());

        // If we are operating in type-oracle mode and fragment is selection, then add similar values as tokens
        // (otherwise, we know it's a projection/relation so we don't need to find values)
        boolean findSimValues = !this.typeOracle || fragType.equalsIgnoreCase("s");

        // Add numeric predicates
        if (findSimValues) {
            for (Attribute attr : this.db.getNumericAttributes()) {
                cands.add(new NumericPredicate(attr, op, Double.valueOf(numericToken)));
            }
        }

        System.out.println("Cands for '" + op + " " + numericToken + "': " + cands.size());

        return cands;
    }

    private List<MatchedDBElement> matchNumericCandidates(List<String> tokens, Set<DBElement> numericCands, String fragType) {
        List<MatchedDBElement> matchedEls = new ArrayList<>();

        for (DBElement cand : numericCands) {
            if (cand instanceof Relation) {
                Relation rel = (Relation) cand;
                double sim = this.sim.sim(rel.getName(), String.join(" ", tokens));

                // If we have a type oracle activated and we know it's a projection, treat as such
                if (this.typeOracle && fragType.equalsIgnoreCase("p")) {
                    matchedEls.add(new MatchedDBElement(rel.getMainAttribute(), sim));
                } else {
                    matchedEls.add(new MatchedDBElement(rel, sim));
                }
            } else if (cand instanceof Attribute) {
                Attribute attr = (Attribute) cand;
                double sim = this.sim.sim(attr.getCleanedName(), String.join(" ", tokens));
                matchedEls.add(new MatchedDBElement(attr, sim));
            } else if (cand instanceof NumericPredicate) {
                NumericPredicate pred = (NumericPredicate) cand;

                double sim = 1.0;
                double root = 1.0;

                if (!tokens.isEmpty()) {
                    sim = this.sim.sim(pred.getAttr().getCleanedName(), String.join(" ", tokens));
                    root++;
                }

                // Check predicate
                try {
                    ResultSet rs = this.db.executeSQL("SELECT IF(EXISTS(SELECT * FROM " + pred.getAttr().getRelation().getName()
                            + " WHERE " + pred.getAttr().getName() + " " + pred.getOp() + " " + pred.getValue()
                            + "), 1, 0) AS result");
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        sim *= 1 - BaselineParams.SQLIZER_EPSILON;
                    } else {
                        sim *= BaselineParams.SQLIZER_EPSILON;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                matchedEls.add(new MatchedDBElement(pred, Math.pow(sim, 1.0 / root)));
            } else {
                throw new RuntimeException("Invalid DBElement type.");
            }
        }
        return matchedEls;
    }

    private List<MatchedDBElement> pruneTopMatches(List<MatchedDBElement> matches) {
        List<MatchedDBElement> pruned = new ArrayList<>();

        double lastScore = 0.0;
        for (int i = 0; i < matches.size(); i++) {
            if (i < BaselineParams.MAX_TOP_CANDIDATES) {
                pruned.add(matches.get(i));
                lastScore = matches.get(i).getScore();
                continue;
            }

            // If we went beyond the MAX_TOP_CANDIDATES, only keep going so long as we have a tie
            if (matches.get(i).getScore() == lastScore) {
                pruned.add(matches.get(i));
            } else {
                break;
            }
        }
        return pruned;
    }

    private boolean executeQueryTask(QueryTask queryTask) {
        System.out.println("== QUERY ID: " + queryTask.getQueryId() + " ==");

        QueryMappings queryMappings = new QueryMappings(this.db, this.joinScore);
        for (FragmentTask fragmentTask : queryTask.getFragmentTasks()) {
            List<String> tokens = new ArrayList<>();
            String numericToken = null;

            for (String token : fragmentTask.getPhrase().split(" ")) {
                if (StringUtils.isNumeric(token)) {
                    numericToken = token;
                } else {
                    tokens.add(token);
                }
            }

            Set<DBElement> cands;
            List<MatchedDBElement> matchedEls;
            if (numericToken == null) {
                cands = this.getTextCandidateMatches(tokens, fragmentTask.getType());
                matchedEls = this.matchTextCandidates(tokens, cands, fragmentTask.getType());
            } else {
                cands = this.getNumericCandidateMatches(numericToken, fragmentTask.getOp(), fragmentTask.getType());
                matchedEls = this.matchNumericCandidates(tokens, cands, fragmentTask.getType());
            }
            matchedEls.sort(Comparator.comparing(MatchedDBElement::getScore).reversed());
            FragmentMappings fragMappings = new FragmentMappings(fragmentTask,
                    this.pruneTopMatches(matchedEls));
            queryMappings.addFragmentMappings(fragMappings);
        }

        List<Interpretation> interps = queryMappings.findOptimalInterpretations();
        int correctFrags = 0;
        int totalFrags = queryTask.size();
        int ties = interps.size() - 1;

        System.out.println("TOTAL SCORE: " + interps.get(0).getScore() + ", TIES: " + ties);
        for (Interpretation interp : interps) {
            System.out.println("--");
            for (int i = 0; i < queryMappings.getFragmentMappingsList().size(); i++) {
                FragmentMappings fragmentMappings = queryMappings.getFragmentMappingsList().get(i);

                String answer = fragmentMappings.getTask().getAnswer();
                MatchedDBElement bestResult = interp.get(i);

                System.out.println(fragmentMappings.getTask().getPhrase() + " :: "
                        + answer + " : " + bestResult);
                if (bestResult.getEl().toString().equals(answer)) {
                    correctFrags++;
                }
            }
        }
        boolean correct = (correctFrags == totalFrags) && (ties == 0);

        System.out.println("== RESULT: " + (correct? "CORRECT" : "WRONG") + " == ");
        System.out.println();

        return correct;
    }

    public void execute(int startAt) {
        int totalTasks = 0;
        int correctTasks = 0;
        for (Map.Entry<Integer, QueryTask> e : this.queryTasks.entrySet()) {
            if (e.getKey() < startAt) continue;

            boolean correct = this.executeQueryTask(e.getValue());
            if (correct) correctTasks++;
            totalTasks++;

            System.out.println("SO FAR: " + correctTasks + "/" + totalTasks);
            System.out.println();
        }

        double accuracyPercent = (double) correctTasks / (double) totalTasks * 100;
        System.out.println("==== FINAL RESULTS ====");
        System.out.println(correctTasks + "/" + totalTasks + " (" + accuracyPercent + "%)");
    }

    public void execute() {
        this.execute(0);
    }

    public static void main(String[] args) {
        String dbHost = args[0];
        Integer dbPort = Integer.valueOf(args[1]);
        String dbUser = args[2];
        String dbPass = args[3].equalsIgnoreCase("null")? null : args[3];
        Boolean typeOracle = Boolean.valueOf(args[4]);
        Boolean joinScore = Boolean.valueOf(args[5]);

        Database database = new Database(dbHost, dbPort, dbUser, dbPass,
                "mas", "data/mas/mas.edges.json", "data/mas/mas.main_attrs.json");
        SQLizer sqlizer = new SQLizer(database, "data/mas/mas_all_fragments.csv", typeOracle, joinScore);

        if (args.length >= 7) {
            sqlizer.execute(Integer.valueOf(args[6]));
        } else {
            sqlizer.execute();
        }
    }
}
