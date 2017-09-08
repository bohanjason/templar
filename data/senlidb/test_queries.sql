select top 50000 * from posts where posts.ViewCount > 10000 ORDER BY posts.ViewCount desc
SELECT avg(DATEDIFF(ww,CreationDate, GETDATE())) FROM Users;
select * from posts
SELECT TOP 20 * FROM users WHERE DisplayName LIKE '%Marinov%'
select * from badges
select * from Comments where Text='*hibernate*'
SELECT DisplayName FROM Users WHERE Id<100000
Select score, parentid from posts where score is not null
select OwnerUserId, Id from Posts where PostTypeId = 2 order by OwnerUserId
select Id, Name from PostTypes where Name='Answer'
select p.Id, p.Body from Posts p inner join PostTags pt on p.Id = pt.PostId where pt.TagId = 17 and p.Id < 1000
select count(*) from ( select u.id from users u join posts p on p.owneruserid = u.id where lower(u.location) like '%russia%' and tags like '%sql%' group by u.id having min(p.creationdate)>='2016-01-01' )x;
SELECT TOP 20 ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE LOWER(Location) LIKE '%bulgar%' ORDER BY Reputation DESC;
select top 15 u.displayname, number_of_answers = count(*), total_score = sum(p.score) from users u join posts p on p.owneruserid = u.id join posts pp on p.parentid = pp.id join posttags pt on pt.postid = pp.id join tags t on t.id = pt.tagid where t.tagname like '%scala%' and p.creationdate > (getdate()-30) and u.location like '%London%' group by u.displayname order by 2 desc;
select top(5) * from posts as p join users u on u.id = p.owneruserid
create table #Total_Tags_Count (postId int, totalTagCount int, numTags int) insert #Total_Tags_Count select Posts.Id, Sum(Tags.Count), Count(*) from Posts inner join PostTags on PostTags.PostId = Posts.Id inner join Tags on Tags.Id = PostTags.TagId where Posts.ParentId is null group by Posts.Id select top 50000 Posts.Id, Posts.Score as postScore, Posts.ViewCount as views, Posts.AnswerCount as answers, Posts.CommentCount as comments, totalTagCount, numTags, Users.Reputation as ownerRep from Posts inner join Users on Users.Id = Posts.OwnerUserId inner join #Total_Tags_Count as ttc on ttc.postId = Posts.Id where Posts.ParentId is null and Posts.CreationDate>='01-01-2013' and Posts.CreationDate<='12-31-2013'
select top 50000 * from posts where posts.ViewCount>1000000 ORDER BY posts.ViewCount
SELECT TOP 10 postid, count(id) FROM PostLinks group by postid ORDER BY count(id) DESC
select DisplayName from Users;
SELECT * FROM Tags WHERE TagName='java';
select top 10000 Posts.id, Posts.body from posts where posts.ViewCount>10000
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE (LOWER(Location) LIKE '%cracow%' OR UPPER(Location) LIKE '%CRACOW%' or Location like '%Cracow%' or LOWER(Location) LIKE '%krakow%' OR UPPER(Location) LIKE '%KRAKOW%' or Location like '%Krakow%') ORDER BY Reputation DESC;
select datediff(day,LastAccessDate,getdate()),Id from Users
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE LOWER(Location) LIKE '%Malaysia%' OR UPPER(Location) LIKE '%Malaysia%' or Location like '%Malaysia%' AND Reputation >= 1000 ORDER BY Reputation DESC;
select top 10 * from PostFeedback where PostId=54136791344
select PostTypeID from Posts Where Id >1
SELECT TOP 2 * FROM Posts WHERE Body LIKE '%sarcastic%'
select creationdate, getdate(), getdate()-creationdate from users where id = 2173917 ;
SELECT age, views from users where views >100
SELECT u.DisplayName'Display Name', u.Reputation'Rep', u.Location'Location' FROM Users u WHERE u.Location LIKE '%Hungary%' ORDER BY 'Rep' DESC
Select top 10 * from Posts Order By ViewCount Desc
SELECT Score as Posts, ViewCount from Posts m;
select Id, score from Posts
select ParentId as [Post Link], count(id) from posts where posttypeid = 2 and len(body) <= 200 and (body like '%scrum%') group by parentid having count(id) > 1 order by count(id) desc;
SELECT TOP 100 * FROM users WHERE 1=1 AND reputation > 100 AND Location LIKE '%Colorado' AND displayname like '%z%'
SELECT DisplayName, a.Id, Location, Reputation, count(b.userid) as BadgesCount From Users a, badges b where a.id=b.userid and Reputation>=550000 group by DisplayName, a.Id, Location, Reputation order by reputation desc
select distinct top 1000 title, body from posts p inner join posttags pt on pt.postid = p.id inner join tags t on pt.tagid = t.id where t.tagname in ('php', 'javascript','jquery')
SELECT TOP 100 * FROM Users
select DATEDIFF(week, p.CreationDate, getdate()) from posts p
select id, posttypeid, creationdate, score, owneruserid from posts where creationdate >= '01/01/2015' and creationdate <= '06/30/2015' and posttypeid=2 and owneruserid in (3010968,187606,187690,189205)
select * from posts where owneruserid = 2173917 and PostTypeId=2 and creationdate > '2015-01-01';
SELECT * FROM Posts WHERE Title LIKE '%Django%';
SELECT TOP 50000 Posts.FavoriteCount, Tags.TagName, Tags.Count FROM Posts JOIN PostTags ON PostTags.PostId = Posts.Id JOIN Tags ON Tags.Id = PostTags.TagId WHERE Posts.Tags LIKE '%c#%' ;
SELECT * FROM POSTS WHERE SCORE > 150 ORDER BY Score;
SELECT Id, AccountId, DisplayName, Reputation, WebsiteUrl, Location FROM Users WHERE Id in (6628959, 6690644, 6678804, 6700305, 6700283, 6609213) ORDER BY reputation DESC
select count(*) from posts; select count(*) from posts where AnswerCount=0;
SELECT * FROM PostFeedback
select Id as [Post Link] from posts where Body like '%http://giphy.com/gifs/%/fullscreen%' and CreationDate >= '1 jan 2015'
SELECT TOP 100 Title FROM Posts WHERE Title LIKE 'Problem%' OR Title LIKE 'problem%' ORDER BY LEN(Title)
select top 50000000000 id, displayname from users order by NEWID()
SELECT * FROM TAGS WHERE TagName = 'c'
select * from ReviewRejectionReasons;
SELECT * FROM Posts
select count(*) from posts where id = 1;
select Id, Score from Posts where ID in (4,6)
select * from PostFeedback where Id=4109;
Select Count(*) From Users
select count(1) from posts where Tags = 'python';
Select * from Users
select count(id) from posts where owneruserid in (select owneruserid from (select owneruserid, count(posts.id) as postNum from posts where CreationDate > '2010-12-31' and CreationDate < '2014-01-01' group by owneruserid )as newt where newt.postNum > 200 ) and PostTypeId=1 and CreationDate > '2010-12-31' and CreationDate < '2014-01-01'
select * from users where displayName like 'Wiktor St%'
select Id, TagName, Count from Tags where Count > 1000 order by Count desc;
select count(*) from posts where tags like '%serverless%'
Select * From Users WHERE LOWER(DisplayName) LIKE '%artur%'
SELECT* FROM Users WHERE LOWER(Location) LIKE 'Canada' AND Reputation >= 1000 ORDER BY Reputation DESC;
Select * from Users where Location like '%Iran%' and Reputation > 2700
SELECT DISTINCT Id, Tags, CreationDate FROM Posts WHERE Tags LIKE '%image-processing%'
SELECT TOP 100 OwnerDisplayName, CreationDate FROM [Posts] ORDER BY CreationDate ASC
SELECT TOP 1 * FROM Posts WHERE PostTypeID = 2
SELECT TOP 20 Posts.Id, Users.Reputation, Tags.Count, Posts.ViewCount FROM Posts INNER JOIN Users ON Users.Id = Posts.OwnerUserId INNER JOIN PostTags ON PostTags.PostId = Posts.ParentId INNER JOIN Tags ON Tags.Id = PostTags.PostId
SELECT Count(*) AS QuestionCount FROM Posts p JOIN PostTags t on p.Id = t.TagId WHERE t.TagId = 1589 AND p.PostTypeId = 1
select * from comments where userid = 2762884 order by creationdate desc
Select id, EmailHash, AboutMe from Users
select top 10 * from users
Select * from Votes v, Posts p where p.OwnerUserId = 507 and '27 Jan 2011' = Convert(Date, v.CreationDate) and v.PostId = p.Id
SELECT Title, Body FROM Posts WHERE Body LIKE '%CJam%' ORDER BY Score
select Id as [Post Link], Body, Score from Posts where Len(Body) > 200
select * from Posts as p where p.id=184618;
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE LOWER(Location) LIKE '%india%' AND Reputation >= 1530 ORDER BY Reputation DESC;
select * from posts;
select COUNT(*) from Users
SELECT TOP 100 sum(Score) as TotalScore, sum(Score)*1000/count(Id) as AvgScore, OwnerUserId as UserName, count(Id) as NumPosts FROM Posts group by OwnerUserId order by sum(Score) DESC;
SELECT TOP 50 p1.Score, p1.Title, p1.Body, p2.Body FROM [Posts] p1 JOIN [Posts] p2 ON (p1.AcceptedAnswerId = p2.Id) WHERE p1.Title LIKE '%VHDL%' ORDER BY p1.Score ASC;
select * from Users where DisplayName = 'CodeJack'
SELECT top 10 * FROM Posts where owneruserid=2357543
select count(*) from posts where posts.ViewCount>15000 and posts.ViewCount<20000
SELECT DisplayName FROM Users LEFT JOIN Posts ON Users.Id=Posts.OwnerUserId
select CASE WHEN (Title NOT LIKE '%magento%') THEN ('Magento ' + Title) ELSE Title END AS Title from posts where title !='';
select * from Posts where Tags like '%powerbi%';
Select tagname, count(*) c from posts p join posttags pt on pt.postid = p.id join tags t on pt.tagid= t.id Where tags like '<sql>' and p.creationdate >= '2014-11-01' and p.creationdate <= '2014-12-01' group by tagname order by c desc
SELECT Tags FROM Posts WHERE Tags = 'sockets'
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE (LOWER(Location) LIKE '%kuala lumpur%' OR UPPER(Location) LIKE '%KUALA LUMPUR%' or Location like '%Kuala Lumpur%' or LOWER(Location) LIKE '%kl%' OR UPPER(Location) LIKE '%KL%') AND Reputation >= 500 ORDER BY Reputation DESC;
select top 50 * from Posts
select count(*),owneruserid from posts where score > 1000 group by owneruserid
select count(*) from Tags
select top 100 p.Title, p.Tags, v.BountyAmount, v.CreationDate from posts p inner join votes v on p.id = v.postid;
select * from users
select Distinct PostId, TagName from PostTags pt , tags t where pt.TagId = t.ID and PostId IN (Select top 50 PostId from PostTags pt inner join tags t on pt.TagId = t.ID where t.TagName In ('r', 'python'))
select * from Tags where TagName = 'isabelle'
SELECT * FROM Users WHERE LOWER(Location) LIKE LOWER('%Yerevan%') AND DisplayName = 'Babl' ORDER BY Reputation DESC;
select top 5000 Id, body, title, tags from posts tablesample(50000 ROWS) where title <> ''
select top 200 id as [Post Link],body,FavoriteCount,score,creationdate,tags from Posts where tags like '%<android>%' order by FavoriteCount desc
select distinct(tags) from Posts order by tags
SELECT Count(*) FROM Posts WHERE OwnerUserId=3052665;
SELECT * FROM Posts WHERE Id < 1000
select top 100 id, score, viewcount, commentcount from posts where posttypeid = 2;
select * from posts where posttypeid=3;
select count(*) from posts where posts.ViewCount > 60000
select * from badges where name like ('A%')
create table #views_ownerRep (PostId int, ViewCount int, Score int, OwnerRep int) insert #views_ownerRep select Posts.Id, Posts.ViewCount, Posts.Score, Users.Reputation from Posts INNER JOIN Users ON Users.Id = Posts.OwnerUserId select top 50000 Score, OwnerRep, Tags.Count as TagCount, TagName from #views_ownerRep vpr INNER JOIN PostTags ON PostTags.PostId = vpr.PostId INNER JOIN Tags ON Tags.Id = PostTags.TagId where ViewCount>0
select distinct u.Id , Reputation, DisplayName, WebsiteUrl, AboutMe, Views from Users u inner join Comments c on c.UserId = u.Id where u.Location like '%rancisco%' and c.Text like '%Node.js%'
SELECT TOP 10000 Id, DisplayName, Reputation, WebsiteUrl, Location, CreationDate, LastAccessDate, AboutMe, Views, UpVotes, DownVotes, ProfileImageUrl, EmailHash, Age, AccountId FROM Users WHERE Location like N'%Kenya%'
SELECT TOP 50 * FROM Posts WHERE Tags LIKE '%javascript%' ;
SELECT COUNT(Location), Location FROM Users GROUP BY Location ORDER BY COUNT(Location) DESC;
SELECT id FROM votetypes
select * from votetypes where id <10
select top 10 Score*1.0/ViewCount as Ratio, Title from Posts order by Score*1.0/ViewCount desc
select * from posts Where score between 10 and 20
SELECT TOP 10 CreationDate FROM Users ORDER BY CreationDate ASC
SELECT TOP 20 * FROM Posts WHERE Tags like '%<android>%' AND PostTypeId=1 ORDER BY Score Desc
select id,tags from posts where posttypeid = 1 and Tags like '%c#>%' and id < 28922954
select distinct u.Id , Reputation, DisplayName, WebsiteUrl, AboutMe, EmailHash, Views from Users u inner join Comments c on c.UserId = u.Id where u.Location like '%israel%'
SELECT Count(*) AS QuestionCount FROM Posts p JOIN PostTags t on p.Id = t.TagId WHERE t.TagId = 1589 AND p.PostTypeId = 1
select count(*) from users where (UpVotes > 10);
SELECT Id FROM Posts WHERE tags ='salt-stack'
Select Age, Sum(UpVotes) From Users Where Age between 30 and 33 group by Age
SELECT Id, Tags FROM Posts WHERE Posts.Tags NOT LIKE '%python%' AND Posts.OwnerUserId = 1663352
select sum(viewcount) from posts where id in ( select parentid from posts where owneruserid = 2173917 and PostTypeId=2 and creationdate > '2015-01-05' and creationdate <= '2015-01-11');
SELECT Reputation FROM Users GROUP BY Reputation ORDER BY Reputation
SELECT * FROM Comments WHERE PostId IN ( SELECT pt.PostId FROM PostTags pt WHERE TagId = 17 );
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE (LOWER(Location) LIKE '%kiev%' OR UPPER(Location) LIKE '%KIEV%' or Location like '%Kiev%' or LOWER(Location) LIKE '%kiev%' OR UPPER(Location) LIKE '%KIEV%' or Location like '%Kiev%') AND Reputation >= 1000 ORDER BY Reputation DESC;
SELECT TOP 100 * FROM Posts
select id,parentid from posts order by id asc;
SELECT DisplayName, Id, Location, Reputation FROM Users order by reputation desc
SELECT Age, COUNT(Age) AS count_per_age FROM Users GROUP BY Age;
select Top 10 Id, Name from PostTypes
select * from comments where text like '%brazilianldsjag.com%'
SELECT Score FROM Posts
select * from Users where displayname like '%Bhargav%'
select top 10 * from Votes
SELECT COUNT(*) FROM Users;
Select Count(*) From Users where CreationDate <= Convert(datetime, '2016-12-16' )
SELECT * FROM posts WHERE Title LIKE 'How%';
SELECT Id AS [Post Link] FROM Posts WHERE Body LIKE 'Try this%'
select [PostId] as [Post Link], s.Id as [Suggested Edit Link] from SuggestedEdits s
SELECT Title, Id, Score, ViewCount FROM Posts WHERE Title like '%jest%'
select top 20 * from Votes where UserId is not null and VoteTypeId NOT IN (5, 8)
SELECT * FROM Users WHERE reputation > 10000;
select count(*) from Posts where AcceptedAnswerId is not null
SELECT us.* FROM Users us WHERE lower(location) like '%hyderabad%' AND lower(AboutMe) like '%java%' ORDER BY Reputation desc;
select displayname, creationdate from users select count(displayname) from users
select top 11 * from users
select avg(score) from posts where posttypeid=2 and owneruserid=184706
DECLARE @NOW DateTime Set @NOW = ( SELECT top 1 cast(Posts.CreationDate as date) AS Date FROM Posts ORDER BY Posts.CreationDate ); SELECT @NOW;
select datediff(w, deletiondate, getdate()) as [deletion] , count(*) as [delete count] from postswithdeleted where deletiondate is not null group by datediff(w, deletiondate, getdate()) order by 1
select text, postid as [Post Link] from Comments, Posts Where comments.postid = posts.id and comments.userid = posts.ownerUserId and text like '%I don%agree' order by comments.creationdate desc
select name from badges
SELECT count(*) FROM Posts GROUP BY OwnerUserId HAVING count(OwnerUserId) < 10
select Id,CreationDate,LastAccessDate,datediff(day,CreationDate,LastAccessDate) from Users
select top 1 displayname, count(*) from users group by displayname
select * from Users where DisplayName = 'Alex A.'
select * from PostLinks
SELECT TOP 10 ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation, Location, age FROM Users WHERE LOWER(Location) LIKE '%iran%' OR UPPER(Location) LIKE '%ir' ORDER BY Reputation DESC;
SELECT TOP 50 Title, Body FROM Posts ORDER BY CreationDate;
select EmailHash from Users where Id = 2899618
select SourceTagName, TargetTagName from TagSynonyms
select top 10 * from posts where body like '%CDC%'
select count(u.Id) as "total users", sum(u.UpVotes) as "total upvotes", sum(u.DownVotes) as "total downvotes" FROM Users u
select * from votes
SELECT TagId, Tags FROM Posts FULL JOIN PostTags ON Posts.Id = PostTags.PostId
select * from Users where reputation > 200
SELECT * FROM Posts WHERE (Tags LIKE '%compiler-errors%' OR Tags LIKE '%compiler-warnings%') AND Body LIKE '%explain%'
select count(*) from Posts where Tags like 'iot'
select top 10 DisplayName, Reputation from Users where Reputation > 100000
select top(1000) * from Posts;
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE LOWER(Location) LIKE '%turkey%' OR UPPER(Location) LIKE '%TURKEY%' or Location like '%Turkey%' AND Reputation >= 1000 ORDER BY Reputation DESC;
select count(*) from posts where body='test'and id>1
select * from sys.databases
SELECT TOP 100 Id, DisplayName, Reputation, Location, Views, UpVotes, DownVotes, Age FROM Users WHERE Location LIKE '%Sofia%' or Location LIKE '%Plovdiv%' or Location LIKE '%Varna%' or Location LIKE '%Bulgaria%' ORDER BY Reputation DESC
select count(*) from POSTS
SELECT COUNT(*) FROM posts WHERE Tags LIKE '%<compiler-errors>%'
SELECT pt.TagId, COUNT(*) AS total FROM PostTags pt INNER JOIN Tags t ON t.Id = pt.TagId GROUP BY pt.TagId ORDER BY total;
select * from users where Reputation = 2059;
SELECT Location FROM Users WHERE Location like '%chennai%' GROUP BY Location
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation,Location FROM Users WHERE Location ='Kerala' and Reputation >1000
SELECT TOP 10 * FROM posts WHERE Id=191316
SELECT COUNT(DISTINCT(TagName)) FROM Tags;
SELECT AccountId, Age, Location FROM Users WHERE Age = 28 AND Location = 'Cracow';
select * from Tags t where t.Count > 1000
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Reputation FROM Users WHERE LOWER(Location) LIKE '%jaipur%' AND Reputation >= 1000 ORDER BY Reputation DESC;
select Text AS Inhalt from Comments where Comments.Id=1000;
SELECT COUNT(DISTINCT(Id)) FROM Tags;
select distinct owneruserid, ownerdisplayname from Posts where owneruserid = 1
SELECT * FROM Comments WHERE Text LIKE '% new File("resources/images/", image)%';
select * from posts where posttypeid = 1 and title like '%female%'
select TOP(100) * from posts
SELECT TOP (20) * FROM POSTS
select p.id as postId, p.ownerdisplayname, pt.name as postType, vt.name as voteType from posts p join posttypes pt on pt.id = p.posttypeid join votes v on v.postid = p.id join votetypes vt on vt.id = v.votetypeid where p.id = 14628012 or p.parentid = 14628012
select top 100 * from posts where owneruserid in (5468160, 4934748);
SELECT TOP 100 Tags, AVG(CommentCount) AS AvgCom FROM Posts GROUP BY Tags ORDER BY AvgCom DESC
select top 10000 Posts.id, Posts.body from posts where posts.ViewCount>10000
SELECT TOP 10 Body FROM Posts WHERE Tags LIKE '%salt%' ORDER BY Score DESC
select Title, Score, ViewCount from Posts order by Score desc, ViewCount asc
SELECT Posts.Title As [Post link], ViewCount, tags from Posts where Title like '%Exception%' and ViewCount> 40000 order by ViewCount desc
select id, tagname from tags;
select top 50000 * from posts where posts.ViewCount < 10343 AND posts.ViewCount > 0 ORDER BY posts.ViewCount DESC
select CreationDate, ViewCount, Tags, FavoriteCount from Posts
select Id FROM Badges
select top 10000 Posts.id, Posts.body from posts where posts.ViewCount>10000
select AVG(Score) from Posts where Id >200 and Id<300;
SELECT CreationDate FROM Users WHERE Reputation < 101 ORDER BY CreationDate ASC
select TargetTagName from TagSynonyms where TargetTagName Like '%java%'
Select Date, Count(Id) From Badges Where Name = 'Constituent' Group By (DatePart(Hour, Date) % 4), Date Order By Date
SELECT DisplayName, Id, Location, Reputation FROM Users ORDER BY Reputation DESC;
SELECT SUM(CommentCount) FROM Posts WHERE ClosedDate > '1.1.2013' AND LastActivityDate < '1.1.2014'
select score, count(score) from posts where score > 10 and score < 100 group by score order by score
Select * From INFORMATION_SCHEMA.COLUMNS Where TABLE_NAME = 'Users'
SELECT * FROM [Users] where DisplayName like '%Undhad%'
select t.TagName, t.Count, e.Body Excerpt from Tags t left join Posts e on t.ExcerptPostId=e.Id where t.Count > 1000
SELECT TOP 1 * FROM POSTS
select top 10000 Posts.id, Posts.body from posts where posts.ViewCount>10000
SELECT * FROM Posts WHERE Tags = 'facebook-analytics'
select * from Comments where PostId IN (sELECT Id from Posts where YEAR(CreationDate)>2014);
SELECT * FROM Posts ORDER BY Posts.Id
Select * from Posts
select id, reputation from users where reputation > 100000;
select * from Users where Location like '%atlanta%' order by Reputation desc
SELECT * FROM Users WHERE ISNUMERIC(DisplayName) = 1
select count(id) from users where creationDate >= '2014-01-01' and creationDate <= '2014-12-31';
select Body from Posts where Body like '%doi.org%';
SELECT * FROM COMMENTS
select top 1 * from Tags where count > 100 order by newid()
SELECT TOP 10 * FROM Comments ORDER BY Score DESC;
select * from CloseAsOffTopicReasonTypes;
SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], Id AS [User Link], Location, Reputation FROM Users WHERE Reputation > 100000 ORDER BY Reputation DESC;
WITH USER_BY_TAG AS ( SELECT ROW_NUMBER() OVER(ORDER BY Reputation DESC) AS [#], u.Id AS [User Link], u.Reputation, TagName, (SELECT CONCAT(Tagname, ' ')) Tagz FROM Tags t INNER JOIN PostTags pt ON pt.TagId = t.id INNER JOIN Posts p ON p.ParentId = pt.PostId INNER JOIN Votes v ON v.PostId = p.Id and VoteTypeId = 2 INNER JOIN Users u ON u.Id = p.OwnerUserId WHERE LOWER(Location) LIKE '%moscow%' AND TagName IN ('c++','stl','boost','opengl') GROUP BY u.id, TagName, u.Reputation ) SELECT * FROM USER_BY_TAG ORDER BY Reputation DESC ;
select Id,TagName from Tags
select * from Users where AboutMe IS NOT null and AboutMe != ''
Select * From Posts Where Title like '%Weka%' Or Body like '%Weka%' And Score >1
select * from comments where userid=5752362 order by creationDate desc
SELECT Count(*) AS QuestionCount FROM Posts p JOIN PostTags t on p.Id = t.TagId WHERE p.PostTypeId != 2
select top 50000 * from posts where posts.ViewCount > 1000000 ORDER BY posts.ViewCount
SELECT TOP(20) DisplayName FROM Users;
SELECT TOP 50 Id, Tags, Body, ViewCount, Score, CreationDate FROM Posts where Tags is not NULL and Year(CreationDate) = 2008 and Month(CreationDate) >= 7 and Month(CreationDate) <= 12
SELECT * FROM POSTS WHERE TAGS = 'awk' and answercount > 10;
select Id as [Post Link], Body, Score from Posts where Title like '%get started%' and ParentId is null
SELECT SUM(Parent.ViewCount) FROM POSTS as Parent, POSTS as P WHERE P.PostTypeId=1 AND P.OwnerUserId = '22656' AND P.ParentId = Parent.Id
create table #comments_per_user (UserId int, NumComments int) insert #comments_per_user select top 20 Users.Id, count(*) from Users inner join Comments on Comments.UserId = Users.Id where Users.LastAccessDate >= '2013-01-01' group by Users.Id create table #posts_per_user (UserId int, NumPosts int) insert #posts_per_user select top 20 Users.Id, count(*) from Users inner join Posts on Posts.OwnerUserId = Users.Id where Users.LastAccessDate >= '2013-01-01' group by Users.Id
SELECT CreationDate FROM Users WHERE Id = -1
select top 100 * from posts p where 1=1 and p.posttypeid=2 and p.owneruserid=1980228 and p.score>0
select * from Posts where Posts.Tags like '%config%';
select * from Posts where Posts.Tags like '<sql>' and Posts.AcceptedAnswerId is NULL and Posts.PostTypeId = 1 and Posts.CreationDate between '2013-01-01' and '2013-12-31'
SELECT * FROM Users ORDER BY LEN(DisplayName) DESC
SELECT Users.DisplayName, Posts.Title FROM Users, Posts WHERE Posts.OwnerUserId = Users.Id AND Posts.ViewCount = (SELECT MAX(ViewCount) FROM Posts)
select id,body as content,title, CreationDate,ClosedDate,Score,LastActivityDate,ViewCount as no_of_views,tags from posts where tags like '%paypal%' and AnswerCount = 0 order by id desc
select * from comments;
Select Id, Score, Body from Posts where ViewCount > 10
Select Id, DisplayName, location, AccountID, WebsiteUrl from Users where location in ( Select distinct(location) from Users where location like '%switzer%')
select OwnerUserId,sum(score) from posts where score > 1000 group by OwnerUserId;
SELECT * FROM Tags where tagname like '%sql%' order by tagname desc
select id, postid, votetypeid, userid, creationdate, bountyamount from votes where postid in (21984272,21948145) order by userid, creationdate, votetypeid;
select Id as [Post Link], Body, Score from Posts where Tags Like '%stakeholder%'
SELECT Score,CreationDate,Tags FROM Posts WHERE Score > 10 OR Tags LIKE 'linux'
SELECT Id, Reputation, DisplayName FROM Users WHERE Reputation > 10000 ORDER BY Reputation;
SELECT TOP 25 Posts.Id AS [Post Link], Posts.OwnerUserId as [User Link], Score FROM Posts WHERE Score <0 AND PostTypeId = 2 ORDER BY Score ASC
select TagName, Count from Tags where TagName = 'angularjs' OR TagName = 'reactjs'
select top 100 Id, DisplayName, UpVotes+DownVotes as Votes, UpVotes, DownVotes from Users order by Votes desc;
SELECT TOP 10 * FROM PostsWithDeleted WHERE DeletionDate IS NOT NULL AND OwnerUserId=130164
select top 20 * from posts;
SELECT DisplayName, Reputation FROM Users WHERE Reputation = ( SELECT Max(Reputation) from Users );
SELECT * FROM Posts where lower(tags) like '%predix%' or lower(title) like '%predix%'
select Id, Reputation, DisplayName, Location from Users where Location like '%Pakistan%' OR Location like '%pakistan%' order by Reputation desc
SELECT * FROM Posts WHERE Id = 4971636;
select count(*) from posts where title like '%semarchy%'
Select Posts.Id, Posts.Title, Posts.Tags from Posts where Posts.Tags like '%bigdata%';
SELECT Id, TagName, Count FROM Tags
select Id, Reputation, Location from Users where Views > 500
select top(10) * from users order by reputation desc
SELECT * FROM PostHistory ph WHERE (ph.PostHistoryTypeId = 4 OR ph.PostHistoryTypeId = 5) AND ph.PostId = 1
select distinct u.Id , u.Location, Reputation, DisplayName, WebsiteUrl, AboutMe, Views from Users u inner join Comments c on c.UserId = u.Id where u.Location like '%ndia%' and u.WebsiteUrl like '%linkedin%'
select * from PostTags
SELECT CommentCount, ClosedDate FROM Posts WHERE ClosedDate > 2013-01-01
select Id, DisplayName from Users
select top 250 Id, PostTypeId, Title, Tags, Score, AnswerCount, FavoriteCount from Posts as [Post Link] where Title is not null and Tags = '<swift>'
SELECT Score, Text, PostId AS [Post Link] FROM Comments WHERE UserId = 241153 ORDER BY Score DESC, Text ASC;
select count(*) ,posts.ViewCount from posts group by posts.ViewCount having count(*)>1
Select top 20 ViewCount, Tags, Tags.Id, Tags.TagName from Posts INNER JOIN PostTags ON PostTags.PostId = Posts.ParentId INNER JOIN Tags ON Tags.Id = PostTags.PostId Where ViewCount > 0
Select TOP 30 * FROM posts WHERE OwnerUserId IN ( SELECT TOP 100 ID from users WHERE Reputation = 1 ) AND PostTypeId = 2 ORDER BY CreationDate DESC
select top 1000 p.CreationDate, p.Body, p.Title, p.ClosedDate from Posts p where p.Title is not null order by p.CreationDate desc
select pl.RelatedPostId FROM Posts p INNER JOIN PostLinks pl ON p.Id = pl.postId WHERE postId = 53734
select DisplayName from users where DisplayName like 'A'
select Body from Posts where id='2097152';
select u.Id as UserID, count(*) as Posts from Users u left join Posts p on u.Id = p.OwnerUserId group by u.Id
select top 10 postid, creationdate, owneruserid, comment from suggestededits;
select * from tags where tagname = 'coldfusion'
select Title, Tags from Posts;