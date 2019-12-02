
# Twitter Project
## Part Two

1. Homework review
2. Navbar
2. User pages
3. Following & unfollowing
    * Changes to data model
    * New controller
    * Front end updates
    * Custom jquery
4. Hashtags
    * Changes to data model
    * Changes to repositories
    * Changes to service
    * Changes to controller
    * New Thymeleaf template

---

### Navbar

So far, we have a few pages up and running. But, there is no easy way to get to one from another. We'll create a navbar that will be displayed at the top of every page. Additionally, the navbar will change based on whether or not the user is logged in!

Inside of `src/main/resources/templates/fragments`, create a new file called `navbar.html`. We will put all the code for our navar in here. Then, we can simply include the fragment on other pages so that it gets displayed everywhere.

Let's start out with the navbar container, which is a component of bootstrap. Inside the container, we'll add a navbar header where we include the name of our website. To make it fancy, we'll add the Twitter logo from Font Awesome and set the color to the official shade of Twitter blue.
```html
<nav class="row navbar navbar-inverse">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="#" style="color: #00aced"><i class="fab fa-twitter"></i> Tech Talent Twitter</a>
        </div>
```

Below that, we'll add our navigation links. We only want these links to show for logged in users, so we'll add `sec:authorize="isAuthenticated()"` to the html tag. Now, the entire `<ul>` will only show if the user is authenticated.
```html
        <div id="navbar">
            <ul class="nav navbar-nav" sec:authorize="isAuthenticated()">
                <li><a href="/tweets">Home</a></li>
                <li><a href="/tweets/new">New Tweet</a></li>
            </ul>
```

Then, we'll display the current username as well as a login/logout link. These will be shown on the far right side of the navbar. Again, we toggle based on whether or not the user is authenticated. If the are, we show logout. If they aren't, we show login. Additionally, we pull in the username from the security context!
```html
            <ul class="nav navbar-nav navbar-right">
                <li sec:authorize="isAuthenticated()"><a style="pointer-events: none;color: #00aced">@<span sec:authentication="name"></span></a>
                <li sec:authorize="isAuthenticated()"><a href="../logout">Logout</a></li>
                <li sec:authorize="!isAuthenticated()"><a href="../login">Login</a></li>
            </ul>
        </div>
    </div>
</nav>
```

Now we just need to drop the navbar into each page. In each of the html templates we've created so far, add the fragment at the top of the body directly below the container. If we need to add additional items to our navbar in the future, we can make changes in one spot and they will be reflected across all pages.
```html
<body>
    <div class="container">
        <div th:replace="fragments/navbar"></div>
...
```

---

### User pages
One of the functions of Twitter is that you can view a profile page for a user, which shows some basic information about them along with all of their tweets. Let's get this set up in our app.

First, we need to create a new controller, called `UserController.java` in our `controller` package. In it, we're going to create an endpoint called `/users/{username}`, which will allow us to view a profile for a user with a particular username. This is the first time we've used a path variable in this project. By adding `@PathVariable(value="username") String username` as a parameter to method, we're able to access whatever is in the URL after `/users/` in the variable `username`.

```java
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TweetService tweetService;
    
    @GetMapping(value = "/users/{username}")
    public String getUser(@PathVariable(value="username") String username, Model model) {	
        ...
```

This method needs to call our `userService` to find the user based on their username and then find all tweets that are linked to that user. Then, it can add those tweets to the Model. We also add the user profile to the Model so that we can display some information about them on `user.html`, which we will create next.

```java
        User user = userService.findByUsername(username);
        List<Tweet> tweets = tweetService.findAllByUser(user);
        model.addAttribute("tweetList", tweets);
        model.addAttribute("user", user);
        return "user"
    }
```

We need to create the page for our user profile. In `src/main/resources/templates`, create `user.html`. Include the standard head and navbar fragments. Below the navbar, we are going to create a title for our page. It will include the user's first and last name, as well as their username. We get this information from the Model and access it using Thymeleaf tags.
```html
...
<div th:replace="fragments/navbar"></div>
<h2>Profile for <span th:text="${user.firstName}"></span> <span th:text="${user.lastName}"></span> - <span style="color: #00aced">@<span th:text="${user.username}"></span></span></h2>
```

Next, we're going to display the date that the user joined Twitter. Remember, we already created this field in our `User` model and set its value to be the creation date. So, we just need to pull it out of the Model and do some formatting. Thymeleaf has built-in funtionality to format dates. We just call `#dates.format` and pass in a date object, along with a format string.
```html
<h5>Joined <span th:text="${#dates.format(user.createdAt, 'MMMM dd, yyyy')}"></span></h5>
```

Now, we'll display the tweets for this user. This is going to be identical to what we saw on the main feed page. Here, additional styling has been added to the tweets. This is one possible solution to last night's homework.
```html
<h3>Tweets</h3>	
    <div class="list-group" th:each="tweet:${tweetList}">
        <div class="list-group-item list-group-item-action">
            <div class="d-flex w-100 justify-content-between">
                <h5>@<span th:text="${tweet.user.username}"></span><span th:text="${#dates.format(tweet.createdAt, 'M/d/yy')}"></small></h5>
            </div>
    		<p class="mb-1" th:utext="${tweet.message}"></p>
        </div>
    </div>
</div>
```

Now, let's go back and add a 'no recent tweets' message, just like you did for last night's homework on the main feed. This is how your tweets section should be set up now:
```html
<div th:if="${not #lists.isEmpty(tweetList)}">
    <h3>Tweets</h3>

    ...

</div>
<div th:if="${#lists.isEmpty(tweetList)}">
    <h3>No Recent Tweets</h3>
</div>
```

Now that we can see an individual user's page, we're going to create a page that shows a list of all users, which will be routed to `/users`. First, let's add a controller method in `UserController` to serve up this page. After being triggered by a GET request, this method will find all users via the `UserService`, add them to the Model, and return back `users.html`.

```java
@GetMapping(value = "/users")
public String getUsers(Model model) {
    List<User> users = userService.findAll();
    model.addAttribute("users", users);
    SetTweetCounts(users, model);
    return "users";
}
```

But wait! What is this `SetTweetCounts` method? We're going to add some special functionality to this page to show how many times each user has tweeted. This method will take in a list of users and the Model, and update the Model to include tweet counts. To keep our controller method focused, we're choosing to break out this piece of functionality.

To store tweet counts, we are going to need a HashMap. The key for each entry will be a username, and the value will be an integer representing the number of tweets the user has made.
```java
private void SetTweetCounts(List<User> users, Model model) {
    HashMap<String,Integer> tweetCounts = new HashMap<>();
```

Then, we iterate through each user, getting a list of their tweets and adding the *size* of that list to the HashMap. Finally, we add the `tweetCounts` HashMap to the model.
```java
    for (User user : users) {
        List<Tweet> tweets = tweetService.findAllByUser(user);
        tweetCounts.put(user.getUsername(), tweets.size());
    }
    model.addAttribute("tweetCounts", tweetCounts);
}
```

Now let's create the page that this controller method returns - `users.html`. In the main container, we'll include a title and then a Thymeleaf iterator.
```html
<div class="container">
    <div th:replace="fragments/navbar"></div>
    <h2>Users</h2>
    <div class="list-group" th:each="user:${users}">
```

For each user, we'll show their first name, last name, username, and tweet count. Since we have individual user pages now, we'll make the username a link to that user's profile page. By adding the tag `th:href="@{/users/} + ${user.username}`, we dynamically build a link by getting the user's username. When we display the tweet count, we use `th:text="${tweetCounts.get(user.username)}"` to get the integer value out of the hashmap for the user based on their username. As you can see, we also include bootstrap classes to push some of the content to the right side of the screen and give our page a nice layout.
```html
    <a th:href="@{/users/} + ${user.username}" class="list-group-item list-group-item-action">
        <div class="row">
            <div class="col col-sm-6">
                <h5 class="mb-1" style="color: #00aced"><i class="fab fa-twitter"></i> @<span th:text="${user.username}"></span></h5>	
                <h5 class="mb-1"><span th:text="${user.firstName}"></span> <span th:text="${user.lastName}"></span></h5>
            </div>
            <div class="col col-sm-2 col-sm-offset-4 text-right">
                <h5 class="mb-1"><span th:text="${tweetCounts.get(user.username)}"></span> tweets</h5>
            </div>
        </div>
    </a>
</div>
```

Now we have both pages set up! In the next section, we'll add more functionality to each of them.

---

### Following & unfollowing
One of the major functions of Twitter is that you "follow" other users and their tweets show up on your "feed". We're going to implement this in our app!

Let's first make the necessary changes to our data model to allow this relationship to exist. Open up the `User` class. We need to have a Many to Many relationship. But what will a user be related to? Other users! So first, we'll add the following:

```java
@ManyToMany(cascade = CascadeType.ALL)
@JoinTable(name = "user_follower", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "follower_id"))
private List<User> followers;
```
This sets up a join table called `user_follower` that contains two columns - `user_id` and `follower_id`. A row in this table indicates that the user (with the `user_id`) is being followed by the user whose id matches the `follower_id`.

Since we also want to be able to easily get a list of the users that a particular user is following, we'll include this:
```java
@ManyToMany(mappedBy="followers")
private List<User> following;
```

That's it for the data model. The next time we restart our app, it will automatically create the join table for us. Next, we'll design a followers controller. This controller will contain endpoints that our app can post to in order to follow or unfollow a particular user.

Inside the `controller` package, create a new class called `FollowController`. Inject the `userService` into it.
```java
@Controller
public class FollowController {
    @Autowired
    private UserService userService;
```

We'll first create the follow method, which will be called whenever we make a post request to `/follow/{username}`. This method will cause the currently logged in user to follow the user whose username is included in the URL. We're also going to need access to the HTTP request that was made on the endpoint, so we include that as a parameter to the method.
```java
    @PostMapping(value = "/follow/{username}")
    public String follow(@PathVariable(value="username") String username, HttpServletRequest request) {
```

Next, we call the `userService` to get the currently logged in user as well as the user we want to follow.
```java
        User loggedInUser = userService.getLoggedInUser();
        User userToFollow = userService.findByUsername(username);
```

Then, we get all of the `userToFollow`'s current followers, add the currently logged in user to the list, and then reassign the updated list to `userToFollow`.
```java
        List<User> followers = userToFollow.getFollowers();
        followers.add(loggedInUser);
        userToFollow.setFollowers(followers);
```

Finally, we save `userToFollow` to flush our changes to the database. Instead of returning the user to a new page, we actually return them back to the page that they made the request from - the "Referer". The only thing that will change on the page they are viewing when they make a follow request is the following status, a feature that we'll add soon.
```java
        userService.save(userToFollow);
        return "redirect:" + request.getHeader("Referer");
    }
```

Our unfollow method will be very similar. The only difference is that instead of adding the currently logged in user to the list, we remove it.
```java
    @PostMapping(value = "/unfollow/{username}")
    public String unfollow(@PathVariable(value="username") String username, HttpServletRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        User userToUnfollow = userService.findByUsername(username);
        List<User> followers = userToUnfollow.getFollowers();
        followers.remove(loggedInUser);
        userToUnfollow.setFollowers(followers);
        userService.save(userToFollow);
        return "redirect:" + request.getHeader("Referer");
    }
}
```

This follow/unfollow functionality will be utilized by pressing a button, which lives inside a form and makes a POST request to one of the two endpoints. Let's first add it to our `user.html` page. At the top of the bootstrap container, right below the navbar, add the following code.
```html
<form th:action="@{/follow/} + ${user.username}" th:if="${!following}" method="post">
    <button style="float:right" type="Submit" class="btn btn-lg btn-success">Follow</button>
</form>
<form th:action="@{/unfollow/} + ${user.username}" th:if="${following}" method="post">
    <button id="unfollow_btn" style="float:right" type="Submit" class="btn btn-lg btn-primary">Following</button>
</form>
```

As you can see, we have two separate buttons/forms, but we use the `th:if` Thymeleaf tag to only display one of them based on whether or not the user is being followed by the currently logged in user. But, we need to add the `following` boolean to our Model so that we can access it here.

In the `UserController`'s `getUser` method, add the following. We iterate through the list of users that are being followed by the currently logged in user to see if the user whose profile we are viewing is one of them. Then, we add either true or false to the Model.
```java
User loggedInUser = userService.getLoggedInUser();
List<User> following = loggedInUser.getFollowing();
boolean isFollowing = false;
for (User followedUser : following) {
    if (followedUser.getUsername().equals(username)) {
        isFollowing = true;
    }
}
model.addAttribute("following", isFollowing);
```

Now if we view a user profile, we see whether or not we are currently following them and can follow/unfollow them. Now try going to the profile page of the currently logged in user. There's an issue here! We can follow ourselves! That doesn't make sense. Let's add a special condition so that this button doesn't appear if it's the currently logged in user's page.

In the same controller method, we're going to generate a boolean that indicates whether or not the profile page that is being returned belongs to the currently logged in user, and add it to the Model.
```java
boolean isSelfPage = loggedInUser.getUsername().equals(username);
model.addAttribute("isSelfPage", isSelfPage);
```

If this boolean is true, we don't want to display either of the follow/unfollow buttons. So, let's add some additional boolean logic to our `th:if` tags in `user.html`.
```html
... th:if="${!following && !isSelfPage}" ...

... th:if="${following && !isSelfPage}" ...
```
Now the problem is taken care of. Let's add the same follow/unfollow buttons to `users.html` so that we can easily follow or unfollow lots of users at once!

First, we'll update our controller method. In `UserController`'s `getUsers` method, we need to add logic to check if the currently logged in user is following each user on the page. Add these three lines to the method:
```java
User loggedInUser = userService.getLoggedInUser();
List<User> usersFollowing = loggedInUser.getFollowing();
SetFollowingStatus(users, usersFollowing, model);
```

Let's implement that `SetFollowingStatus` method, which adds a HashMap to the Model. First, we declare the HashMap, where each pair contains a string key, the username, and a boolean value, indicating whether or not the currently logged in user is following them.
```java
private void SetFollowingStatus(List<User> users, List<User> usersFollowing, Model model) {
    HashMap<String,Boolean> followingStatus = new HashMap<>();
```

Now we iterate though each user and check to see whether or not they're being followed. We add each result to the HashMap.
```java
    for (User user : users) {
        if(usersFollowing.contains(user)) {
            followingStatus.put(user.getUsername(), true);
        }else if (!user.getUsername().equals(username)) {
            followingStatus.put(user.getUsername(), false);
    	}
    }
```

Then, we simply add the HashMap to the model. Now it will be available in our Thymeleaf template!
```java
    model.addAttribute("followingStatus", followingStatus);
}
```

Now let's make the changes to `users.html`. We want the button to appear once for each user, so it needs to go inside our Thymeleaf for loop. Our `th:if` conditions have the same logic as before, but we have to change how we get our values. If the `followingStatus` hashmap contains a false value for the user, we show the follow button. If it is true, we show the unfollow button. To handle the special case of not letting a user follow themself, we add `followingStatus.containsKey(user.username)` to our boolean condition for each form. If the HashMap doesn't contain a key for the user, we know it must be the currently logged in user.
```html
...
<h5 class="mb-1"><span th:text="${tweetCounts.get(user.username)}"></span> tweets</h5>
<form th:action="@{/follow/} + ${user.username}" th:if="${followingStatus.containsKey(user.username) && !followingStatus.get(user.username)}" method="post">
    <button style="float:right" type="Submit" class="btn btn-md btn-success">Follow</button>
</form>
<form th:action="@{/unfollow/} + ${user.username}" th:if="${followingStatus.containsKey(user.username) && followingStatus.get(user.username)}" method="post">
    <button id="unfollow_btn" style="float:right" type="Submit" class="btn btn-md btn-primary">Following</button>
</form>
...
```

Now, wouldn't it be cool if we made the following button dynamic? A button that says 'following' doesn't make it clear that pressing it will cause you to unfollow the user. Let's use jquery to change this button whenever it's hovered over. We want it to turn red and change to say 'unfollow'. If we look back at our html code, we included an id on the unfollow button `unfollow_btn`. This gives us a way to select it. Also, we've already created a `custom.js` file and linked it to this page by importing the script in the head.

In `src/main/resources/static/custom.js`, add the following code:
```js
$(document).ready(function() {
  $('#unfollow_btn').hover(function(){
    $(this).removeClass('btn-primary');
    $(this).addClass('btn-danger');
    $(this).html("Unfollow");
  }, function(){
    $(this).html("Following");
    $(this).removeClass('btn-danger');
    $(this).addClass('btn-primary');
  });
})
```

This jquery code causes the button to change whenever it's hovered over, and then change back as soon as the hover ends. This code will run on both of our user pages! Run the app and checkout how cool that is.

---

### Hashtags

A major aspect of Twitter that we haven't yet talked about is hashtags. Essentially, hashtags let users know that they're talking about the same subject as other users.

So if your tweet has #springboot within the message, you should be able to click on that phrase (as a link), and see every other tweet that contains that  hashtagged-phrase.

Hashtags will not only be phrases within Tweets, they will also be a resource unto themselves. Let's start by adding a new data model. In the `model` package, create `Tag.java`. We know that a hashtag will need an id and a phrase.

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Tag {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tag_id")
    private Long id;
	
    private String phrase;
```

Now we need to figure out how hashtags relate to tweets. A tweet can have multiple hashtags in it. And, of course, the whole point of hashtags is that the same one can appear in multiple tweets. So we have another Many-to-Many relationship. Let's add the folowing to our model:
```java
    @ManyToMany(mappedBy = "tags")
    private List<Tweet> tweets;
}
```

In our `Tweet` model, we need to define the relationship and set up the join table.
```java
@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
@JoinTable(name = "tweet_tag", joinColumns = @JoinColumn(name = "tweet_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
private List<Tag> tags;
```

We now have a join table called `tweet_tag`. Each entry will contain a `tweet_id` and a `tag_id`, indicating that a particular tweet contains a particular hashtag.

Next, let's set up a repository for hashtags. In the `repository` package, create `TagRepository.java`. We'll keep this pretty simple. The only functionality we need is the ability to get a tag based on its phrase.
```java
@Repository
public interface TagRepository extends CrudRepository<Tag, Long>  {
    Tag findByPhrase(String phrase);
}
```

Now we need to handle the actual logic to create and load hashtags. This will all exist inside of `TweetService`, which we knew was going to get more complex. This is the functionality we need to add.

1. When a request is made to `TweetService` to create a tweet, we need to determine if the tweet's message contains hashtags. If it does, for each hashtag we need to link that tweet to the corresponding hashtag. If it's the first time somebody has used that hashtag, we first have to create the hashtag itself.

2. When a request is made to `TweetService` to load tweets, we need to modify the messages of the returned tweets so that they include links to the hashtag pages. Essentially, we want to make the hashtags "clickable".

By encapsulating all this logic in the service, we don't have to change anything in the controller or existing html pages. Let's start with the first piece of functionality - creating tweets. Create a new method called `handleTags(Tweet tweet)`. When a request is made to create a tweet, the tweet will have to pass through this method before it gets sent to the repository and saved to the database.
```java
private void handleTags(Tweet tweet) {
```

First, let's instantiate an ArrayList to hold our hashtags.
```java
    List<Tag> tags = new ArrayList<Tag>(); 
```

This method needs to identify each hashtag in the tweet message. So, we're going to do some regular expression matching to find each word that begins with "#". Luckily, there are some built in classes to help us do this in Java.
```java
    Pattern pattern = Pattern.compile("#\\w+");
    Matcher matcher = pattern.matcher(tweet.getMessage());
```

The Pattern class defines the pattern we're looking for and the Matcher class finds each occurrence of the pattern in the tweet message. Next, the Matcher class allows us to loop through each of the matches that were found. This loop will run once for each hashtag in the tweet.
```java
    while (matcher.find())
    {
```

In this loop, we'll first extract the hashtag phrase from the matcher object. We don't want to include the hashtag symbol in the phrase, so we use substring to ignore it. Additionally, we convert to lowercase so that all hashtags are stored in the database that way, and so that we can link tweets to a given hashtag regardless of what kind of capitalization was used in the tweet. For instance, #spring and #SPRING will be linked to the same hashtag.
```java
        String phrase = matcher.group().substring(1).toLowerCase();
```

Next, we check to see if the hashtag phrase already exists in the database or not. We never want to end up with duplicate rows for the same hashtag because then we couldn't view all tweets with a given hashtag! If the hashtag doesn't exist yet, we create it.
```java
        Tag tag = tagRepository.findByPhrase(phrase);
        if(tag == null) {
            tag = new Tag();
            tag.setPhrase(phrase);
            tagRepository.save(tag);
        }
```

Then, we add the tag to our list of tags. Outside the loop, once each tag has been added, we set the tags for our tweet to be the list we just built.
```java
        tags.add(tag);
    }
    tweet.setTags(tags);
}
```

Now, let's make a small change to our `save` method so that it calls our `handleTags` method.
```java
public void save(Tweet tweet) {
    handleTags(tweet);
    tweetRepository.save(tweet);
}
```

Next, we need to work on the second piece of functionality - loading tweets. In our `TweetService`, lets create a new method called `formatTweets`. This method will take in a list of tweets and return back a modified list that contains hashtag links. Later, we will add to this method.
```java
private List<Tweet> formatTweets(List<Tweet> tweets) {
    addTagLinks(tweets);
}
```

Now, let's create the method `addTagLinks`, which is called by `formatTweets`. This method takes in a list of tweets and for each tweet, modifies the message so that it contains html links for each hashtag, rather than plain text. For example, a tweet message would go into the method looking like this:
```
I love #Java
```

And come out looking like this:
```
I love <a class=\"tag\" href=\"/tweets/java\">#Java</a>
```

We'll start our by declaring a Pattern object to look for hashtags in the tweet message, like we saw before.
```java
private void addTagLinks(List<Tweet> tweets) {
    Pattern pattern = Pattern.compile("#\\w+");
```

Now we cycle through each tweet and find all hashtags in the tweet's message.
```java
    for(Tweet tweet: tweets) {
        String message = tweet.getMessage();
        Matcher matcher = pattern.matcher(message);
```

Next, we add each matched hashtag to a set.
```java
        Set<String> tags = new HashSet<String>();
        while(matcher.find()) {
            tags.add(matcher.group());
        }
```

Then the main logic comes in. For each tag, we replace the plain text tag with a hyperlinked tag. And finally, update the tweet's message.
```java
        for(String tag : tags) {
            message = message.replaceAll(tag, "<a class=\"tag\" href=\"/tweets/" + tag.substring(1).toLowerCase() + "\">" + tag + "</a>");
        }
        tweet.setMessage(message);
    }
}
```

For each of our service methods that load tweets from the repository, we need to add this method. To make it easy, we'll wrap it around the return statement.
```java
public List<Tweet> findAll() {
    List<Tweet> tweets = tweetRepository.findAllByOrderByCreatedAtDesc();
    return formatTweets(tweets);
}
```

Remember to make this change for all of the methods!

To wrap up our hashtag feature, we need to create the functionality to show a page that has all the tweets for a given hashtag. This is the page that we linked to in the generated html. The URL format is `/tweets/{tag}`.

Let's start at the repository layer. In our `TweetRepository`, we need to add a method to get all tweets that contain a given hashtag.
```java
List<Tweet> findByTags_PhraseOrderByCreatedAtDesc(String phrase);
```

Remember, the text we use in the method name actually builds the SQL query. So we have to be very particular about the formatting.

Next, the service layer. In `TweetService`, add a method that calls our newly created repository method. Since this is a method that loads tweets, it has to include our `formatTweets` method.
```java
public List<Tweet> findAllWithTag(String tag){
    List<Tweet> tweets = tweetRepository.findByTags_PhraseOrderByCreatedAtDesc(tag);
    return formatTweets(tweets);
}
```

And now, the controller. In `TweetController`, we need to create a method that is called whenever we make a GET request to `/tweets/{tag}`.
```java
@GetMapping(value = "/tweets/{tag}")
public String getTweetsByTag(@PathVariable(value="tag") String tag, Model model) {
```

Just like with previous controller methods, we add the `@PathVariable` annotation to the `String tag` parameter so that it gets set to whatever is in the URL. This method just needs to call our service and add all of the tweets to the model, along with the hashtag we're interested in.
```java
    List<Tweet> tweets = tweetService.findAllWithTag(tag);
    model.addAttribute("tweetList", tweets);
    model.addAttribute("tag", tag);
    return "taggedTweets";
}
```

The last thing to do is create the HTML page that our controller returns. In `src/main/resources/templates`, create `taggedTweets.html`. As always, start with the standard head. In the body, we'll include a title that shows the hashtag we're looking at.
```html
<body>
    <div class="container">
        <div th:replace="fragments/navbar"></div>
        <h2>Tweets containing #<span th:text="${tag}"></span></h2>
```

We are not including the hashtag symbol in the URL or in the Model attribute, so we add the symbol manually in our title. After that, we just reuse the code from our other pages that display tweets. 
```html
        <div class="list-group" th:each="tweet:${tweetList}">
            <div class="list-group-item list-group-item-action">
                <div class="d-flex w-100 justify-content-between">
                    <a class="username" th:href="@{/users/} + ${tweet.user.username}"><h5>@<span th:text="${tweet.user.username}"></span></a>
                    <small th:text="${#dates.format(tweet.createdAt, 'M/d/yy')}"></small></h5>
                </div>
                <p class="mb-1" th:utext="${tweet.message}"></p>
            </div>
        </div>
```

Finally, just in case a user manually types in a URL and views the page for which no tweets exist, we'll add in the appropriate message.
```html
        <div th:if="${#lists.isEmpty(tweetList)}">
            <h3>No tweets contain this hashtag</h3>
        </div>
    </div>
</body>
```
