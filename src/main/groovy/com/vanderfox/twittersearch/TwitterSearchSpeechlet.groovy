package com.vanderfox.twittersearch

import com.amazon.speech.slu.Intent
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.LaunchRequest
import com.amazon.speech.speechlet.Session
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import com.amazon.speech.speechlet.Speechlet
import com.amazon.speech.speechlet.SpeechletException
import com.amazon.speech.speechlet.SpeechletResponse
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.Reprompt
import com.amazon.speech.ui.SimpleCard
import com.twitter.Extractor
import org.springframework.social.twitter.api.SearchResults
import groovy.transform.CompileStatic
import groovyx.net.http.RESTClient
import net.sf.json.JSON
import net.sf.json.groovy.JsonSlurper
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import org.springframework.social.twitter.api.Tweet
import org.springframework.social.twitter.api.Twitter
import org.springframework.social.twitter.api.impl.TwitterTemplate;



/**
 * Created by Lee Fox and Ryan Vanderwerf on 3/18/16.
 */
/**
 * This app shows how to connect to twitter with Spring Social, Groovy, and Alexa.
 */
@CompileStatic
public class TwitterSearchSpeechlet implements Speechlet {
    private  static final Logger log = LoggerFactory.getLogger(TwitterSearchSpeechlet.class);

    static String CONSUMER_KEY = ""
    static String CONSUMER_SECRET = ""
    static String ACCESS_TOKEN = ""
    static String ACCESS_TOKEN_SECRET = ""
    static TwitterTemplate twitter

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        loadCredentials()
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        Slot query = intent.getSlot("SearchTerm")
        Slot count = intent.getSlot("Count")

        if ("TwitterSearchIntent".equals(intentName)) {
            return getTwitterSearchResponse(query, count);
        } else if ("TwitterTimelineIntent".equals(intentName)) {
            return getTwitterTimelineResponse(query, count);
        } else if ("TwitterMentionIntent".equals(intentName)) {
            return getTwitterMentionResponse(query, count);
        } else if ("TwitterMyLatestsPostsIntent".equals(intentName)) {
            return getTwitterMyLatestPostsResponse(query, count);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Say search and a keyword and I'll search Twitter for relevant tweets.";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("TwitterSearch");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getTwitterMyLatestPostsResponse(Slot query, Slot count) {

        loadCredentials()

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Twitter Search Results");

        String searchTerm = query.getValue()
        String countString = count.getValue()
        int countInt = 1;
        if (countString && !"".equals(countString) && countString.isNumber()) {
            countInt = countString.toInteger();
        }
        def speechText = "Your latest tweets are:\n"
        String cardText = "Your latest tweets are:\n"
        try {
            List<Tweet> tweets = twitter.timelineOperations().getUserTimeline(countInt);
            tweets.eachWithIndex { tweet, index ->
                if (index == 0) {
                    speechText += "First. Tweet:\n"
                    cardText += "First. Tweet:\n"
                } else {
                    speechText += "Next. Tweet:\n"
                    cardText += "Next. Tweet:\n"
                }
                def tweetText = tweet.getText()
                tweetText = cleanupVerbalText(tweetText)

                speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

            }
        } catch (Exception e) {
            speechText = "Sorry.  I had some problems getting information from Twitter.  The exception message was ${e.getMessage()}"
        }

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
        card.setContent(cardText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private void loadCredentials() {
        final Properties properties = new Properties();
        try {
            InputStream stream = TwitterSearchSpeechlet.class.getClassLoader()getResourceAsStream("springSocial.properties")
            properties.load(stream);
            ACCESS_TOKEN = properties.get("accessToken")
            ACCESS_TOKEN_SECRET = properties.get("accessTokenSecret")
            CONSUMER_KEY = properties.get("consumerKey")
            CONSUMER_SECRET = properties.get("consumerSecret")
            twitter = new TwitterTemplate(CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
        } catch (e) {
            log.error("Unable to retrive twitter credentials. Please set up a springSocial.properties")
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getTwitterTimelineResponse(Slot query, Slot count) {
        loadCredentials()

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Twitter Search Results");

        String searchTerm = query.getValue()
        String countString = count.getValue()
        int countInt = 1;
        if(countString && !"".equals(countString) && countString.isNumber()) {
            countInt = countString.toInteger();
        }
        def speechText = "Your timeline is:\n"
        String cardText = "Your timeline is:\n"
        try {
            List<Tweet> tweets = twitter.timelineOperations().getHomeTimeline();
            tweets[1..countInt].eachWithIndex { tweet, index ->
                if(index == 0) {
                    speechText += "First. Tweet:\n"
                    cardText += "First. Tweet:\n"
                } else {
                    speechText += "Next. Tweet:\n"
                    cardText += "Next. Tweet:\n"
                }
                def tweetText = tweet.getText()
                tweetText = cleanupVerbalText(tweetText)
                speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

            }
        } catch (Exception e) {
            speechText = "Sorry.  I had some problems getting information from Twitter."
        }

        // Create the plain text output.
        speech.setText(speechText);
        card.setContent(cardText);

        return SpeechletResponse.newTellResponse(speech, card);
    }


    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getTwitterMentionResponse(Slot query, Slot count) {
        loadCredentials()
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Twitter Search Results")

        String searchTerm = query.getValue()
        String countString = count.getValue()
        int countInt = 1;
        if(countString && !"".equals(countString) && countString.isNumber()) {
            countInt = countString.toInteger();
        }
        def speechText = "Your latest mentions are:\n"
        String cardText = "Your latest mentions are:\n"
        try {
            List<Tweet> tweets = twitter.timelineOperations().getMentions(countInt);
            tweets.eachWithIndex { tweet, index ->
                if(index == 0) {
                    speechText += "First. Tweet:\n"
                    cardText += "First. Tweet:\n"
                } else {
                    speechText += "Next. Tweet:\n"
                    cardText += "Next. Tweet:\n"
                }
                String tweetText = tweet.getText()
                tweetText = cleanupVerbalText(tweetText)

                speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

            }
        } catch (Exception e) {
            speechText = "Sorry.  I had some problems getting information from Twitter."
        }

        // Create the plain text output.
        speech.setText(speechText);
        card.setContent(cardText);

        return SpeechletResponse.newTellResponse(speech, card);

    }


    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getTwitterSearchResponse(Slot query, Slot count) {
        loadCredentials()
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Twitter Search Results");

        String searchTerm = query.getValue()
        String countString = count.getValue()
        int countInt = 1;
        if(countString && !"".equals(countString) && countString.isNumber()) {
            countInt = countString.toInteger();
        }
        def speechText = "I found ${countInt} tweet${(countInt > 1) ? 's' : ''} for ${searchTerm}:\n"
        String cardText = "I found ${countInt} tweet${(countInt > 1) ? 's' : ''} for ${searchTerm}:\n".toString()
        try {
            if(!searchTerm) {
                speechText = "Please tell me something to search for by saying search and a keyword"
                cardText = speechText
                Reprompt reprompt = new Reprompt();
                reprompt.setOutputSpeech(speech);
                card.setContent(cardText);
                return SpeechletResponse.newAskResponse(speech, reprompt, card);
            } else {
                log.info("searching twitter")
                SearchResults results = twitter.searchOperations().search(searchTerm);
                log.info("finished searching twitter")
                List<Tweet> tweets = results.getTweets();
                tweets[1..countInt].eachWithIndex { tweet, index ->

                    if(index == 0) {
                        speechText += "First. Tweet:\n"
                        cardText += "First. Tweet:\n"
                    } else {
                        speechText += "Next. Tweet:\n"
                        cardText += "Next. Tweet:\n"
                    }
                    String tweetText = tweet.getText()

                    tweetText = cleanupVerbalText(tweetText)

                    speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                    cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

                }
            }
        } catch (Exception e) {
            speechText = "Sorry.  I had some problems getting information from Twitter."
        }

        // Create the plain text output.
        speech.setText(speechText);
        card.setContent(cardText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private String cleanupVerbalText(String tweetText) {
        Extractor extractor = new Extractor()
        List<String> urls = extractor.extractURLs(tweetText)
        urls.each { url ->
            tweetText = tweetText.replaceAll(url, "")
        }
        List<String> hashTags = extractor.extractHashtags(tweetText)
        hashTags.each { tag ->
            tweetText = tweetText.replaceAll(tag, "")
        }
        List<String> mentions = extractor.extractMentionedScreennames(tweetText)
        mentions.each { mention ->
            tweetText = tweetText.replaceAll(mention, "")
        }
        List<String> cashTags = extractor.extractCashtags(tweetText)
        cashTags.each { tag ->
            tweetText = tweetText.replaceAll(tag, "")
        }
        return tweetText
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say search and a keyword, and I will search Twitter for you.";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("TwitterSearch");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    static void main(String[] args) {
        TwitterSearchSpeechlet speechlet = new TwitterSearchSpeechlet()
        speechlet.loadCredentials()
        List<Tweet> tweets = twitter.timelineOperations().getHomeTimeline();

        def tweet = tweets.get(0)
        def tweetText = tweet.getText()
        tweetText = speechlet.cleanupVerbalText(tweetText)
        String speechText = "From ${tweet.getFromUser()} ${tweetText}"
        println("Speech Text ${speechText}")
    }
}
