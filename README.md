# email-file-sender

## Purpose
For creating a new budget using YNAB or my Evercent system, it can be a pain to initially set up all the required information. Not only that, but it can be difficult for me to remember everything required when teaching the method to somebody else. Lastly, there is the issue of not being able to get any of these resources to the people I'm speaking to in a quick manner.
As a result, I created this tool which will allow me to send emails (emails that have been already set up with a particular file attachment, and email body) to people so I can give them these resources in a quick fashion.

## Technology
This is using a number of different AWS services, using Python as the main language to handle the AWS side of things.
* AWS S3
  * [Bucket of Files to Send](https://s3.console.aws.amazon.com/s3/buckets/files-to-send?region=us-east-1&tab=objects)
* AWS Lambda
  * [Getting File List](https://us-east-1.console.aws.amazon.com/lambda/home?region=us-east-1#/functions/get-available-files-to-send)
  * [Sending Emails](https://us-east-1.console.aws.amazon.com/lambda/home?region=us-east-1#/functions/send-files-via-email)
* [AWS API Gateway](https://us-east-1.console.aws.amazon.com/apigateway/main/apis?region=us-east-1)
* [AWS SES (Simple Email Service)](https://us-east-1.console.aws.amazon.com/ses/home?region=us-east-1#/account)

The UI side of things is built as an Android application. 
This app is currently only available locally to me, and not readily available on the Google Play store.

## Process
The Android app allows for a "configured file" to send, a single email recipient to send the email to, and the subject of the email that will be sent, along with a button which will allow for the email to actually be sent.
When the button is hit, an HTTP request is sent to our API, which in turn calls our Lambda function.

The "Sending Emails" lambda looks for the "configured file" selected from the Android app, and looks up the folder of the same name within a particular S3 bucket.
Each of these folders should contain 2 files each:
* config.json = This is a configuration file which consists of the body of the email that should be sent. 
  * Right now, this is the only item in the config file
* xxx.xxx = This is the actual file that should be attached to the email and sent to the user. It can be any file type.

The lambda will continue by assembling the email appropriately, attaching the file to the email, and using Amazon SES to send the email.
