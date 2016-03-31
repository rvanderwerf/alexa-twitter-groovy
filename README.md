# Groovy Alexa Twitter Search

[Latest documentation](https://github.com/rvanderwerf/alexa-twitter-groovy) 
# Build Status
![build status](https://travis-ci.org/rvanderwerf/alexa-twitter-groovy.svg?branch=master)
[Build Details](https://travis-ci.org/rvanderwerf/alexa-twitter-groovy/)
## Branches

## Authors
   Lee Fox @foxinatx
   Ryan Vanderwerf @RyanVanderwerf
   
## Using
### Quick start
To start using this app you'll need to set up a few things:

1. create a twitter account
2. login to twitter apps management (https://apps.twitter.com/) and make yourself a developer
3. click on 'Create New App' -> Fill in app name, website, description leave callback url blank
4. go to consumer key -> manage keys and tokens
5. create consumer key and secret if you don't have one
6. fill in these details in springSocial.properties in the src/main/resources directory
7. Sign up for the Amazon developer program [here](https://developer.amazon.com) if you haven't already
8. Login in to your AWS Console. You can sign up for a free account at [AWS](https://aws.amazon.com)
9. Go to Services / Lambda
10. From here you can manually create a function or let Gradle deploy it for you. Skip the blueprint. If you let Gradle do it skip to step 12
11. Create a new Lamba function. Enter the following: 
        Function: TwitterSearchGroovy 
        handler: "com.vanderfox.twittersearch.TwitterSearchSpeechletRequestStreamHandler"
        role: "lambda_basic_execution"
        Runtime: Java 8
        Memory Size: 512
        Timeout: 60
        File: upload file after your task 'build' from build/libs/vanderfox-alexa-twitter-fat-1.0-SNAPSHOT.jar
12. Run the 'deploy' task in gradle like './gradlew deploy' If your credentials are properly set up in ~/.aws/credentials it will run. If 
       not successful check your credentials there and try again.
13. Log back into your AWS console. Go to services -> lambda. You should see your new lamba function 'TwitterSearchGroovy'
14. Go to event sources -> Pick 'Alexa Skills Kit' as event type.
15. Go back to the 'Code' tab. Copy the ARN value you see here. You will need it in the next stops on the Amazon Developer Console.
16. Click on Apps and Services -> Alexa
17. Click on Alexa Skill Kit / Get Started -> Add New Skill
18. Pick any name and any invocation name you want to start the app on your Echo / Alexa Device
19. Copy the ARN from step 15 for the endpoint (Choose amazon ARN not https). Click next
20. Copy the contents of IntentSchema.json from src/main/resources/ into Intent Schema.
21. Under Custom Slot types, add the Slots Type values from /src/main/resources/slots.json
22. Under Sample Utterances, copy the contents of the file SampleUtterances.txt
23. Hit Next, Skip Configuration and go to Test page and hit Save
24. Copy the application ID on the first tab, and past that into the line that adds supported app IDs.
25. Rerun the 'deploy' task in gradle - it will need the code to understand the app ID when the Echo service calls it.
26. Go back to the Amazon Developer Console, Alexa, and open the skill you made.
27. Click edit and hit next until you are the 'Test' tab. You can test it out there. Make sure there are no errors communicating with the services
28. Now try it on your Echo/Alexa device. Say either 'start' or 'open' and the invocation name you gave the app and follow the prompts!

### Using the app:
Functions of the app:
Say open <invocation name>

1. you can say 'search <X> for <value>'  <X> is the number of tweets you want back. If you don't say a number it defaults to 1. Or you can say 'search for <value>'
2. 'get my timeline'
3. 'get my mentions' or 'get my last <X> mentions
4. 'get last <X> tweets' or 'get my latest tweets'
5.  check the SampleUtterances.txt for all of the latest options