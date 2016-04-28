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

##TWITTER
1. Create a twitter account
2. Login to twitter apps management (https://apps.twitter.com/) and make yourself a developer
3. Click on 'Create New App' -> Fill in app name, website, description leave callback url blank
4. Go to consumer key -> manage keys and tokens
5. Create consumer key and secret if you don't have one


##IDEA / EDITOR
6. Fill in these details in file: src/main/resources/springSocial.properties, do not use quotes around the values

##AMAZON Lambda / Alexa Skill tie together
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
12. Run the 'deploy' task in gradle like './gradlew deploy' If your credentials are properly set up in ~/.aws/credentials it will run. If not successful check your credentials there and try again.
13. Log back into your AWS console. Go to services -> lambda. You should see your new lamba function 'TwitterSearchGroovy'
14. Go to event sources -> Pick 'Alexa Skills Kit' as event type. 
15. Copy the ARN value in the top right of any Lambda Screen. Save it in a text edior, you will need it in the Alexa Skill steps.

##AMAZON Alexa skill / Lambda tie together
16. Click on Apps and Services -> Alexa
17. Click on Alexa Skill Kit / Get Started -> Add New Skill
18. Pick any name and any invocation name you want to start the app on your Echo / Alexa Device
19. Copy the ARN from step 15 for the endpoint (Choose amazon ARN not https). Click next

##IDEA / EDITOR -> FILES COPIED TO AMAZON ALEXA SKILL IN AWS
20. Copy the contents of src/main/resources/IntentSchema.json into Intent Schema.
21. Under Custom Slot types, follow the instructions found inside of /src/main/resources/slots.txt
22. Under Sample Utterances, copy the contents of the file src/main/resources/SampleUtterances.txt
23. Hit Next, Skip Configuration and go to Test page and hit Save

## AMAZON ALEXA SKILL ID Copied to IDEA / EDITOR / NEW JAR Deployed to Lambda
24. Copy the application ID on the first tab 'SKILL INFORMATION', and paste that into file src/main/resources/springSocial.properties, do not use quotes around the value.
25. Rerun the 'deploy' task in gradle - it will need the code to understand the app ID when the Echo service calls it.

## AMAAZON Alexa Skill Test
26. Go back to the Amazon Developer Console, Alexa, and open the skill you made.
27. Click edit and hit next until you are the 'Test' tab. You can test it out there. Make sure there are no errors communicating with the services

## ECHO Test
28. Now try it on your Echo/Alexa device. Say either 'start' or 'open' and the invocation name you gave the app and follow the prompts!

### Using the app:
Functions of the app:
Say open <invocation name>

1. you can say 'search <X> for <value>'  <X> is the number of tweets you want back. If you don't say a number it defaults to 1. Or you can say 'search for <value>'
2. 'get my timeline'
3. 'get my mentions' or 'get my last <X> mentions
4. 'get last <X> tweets' or 'get my latest tweets'
5.  check the SampleUtterances.txt for all of the latest options