## NoteBot

Basically it is a chat-based reminder with simple notification configuration. I wanted to make it for a long time since I find standard tools like Reminders on iOS to be too complicated if I need a notification about something trivial.
It can also be used to remember some information.

The reason I chose Kotlin for this project was simply because I wanted to try it. I enjoyed the idea of explicit null-values, and overall writing in Kotlin was an interesting experience, however using [Exposed](https://github.com/JetBrains/Exposed) for Database communication was not. The library feels very unnatural at times, like with eager initializations ([example](https://github.com/JetBrains/Exposed/issues/656)), so I wouldn't consider using it in the future.

#### My deployed bot:
https://t.me/n00te_bot

#### How to deploy:
1. Create your bot with [BotFather](https://t.me/BotFather) and define 3 commands: <code>/timezone</code>, <code>/disable</code>, <code>/getnotes</code>. You should also remember your bot's token received from BotFather, you will need it in step 3
2. Compile the project with gradle <code>gradle build</code>
3. To start the bot execute <code>java -jar note.jar [BOT_TOKEN]</code> command. Insert your token from step 1 instead of [BOT_TOKEN]

#### How to use when deployed:
1. User should set his current timezone with <b>/timezone</b> command
2. User types a message. a dialog with bot is initiated, where bot asks user to provide the notification time. When bot gets the notification time, the note is saved to the Database
3. <b>/disable command</b> - disables all user's notifications
4. <b>getnotes command</b> - prints all existing user's notes. When user selects one the printed notes, more detailed information is printed about the selected note with buttons to delete it or to change the notification time

#### TODO Features:
1. Disabling/enabling notifications for individual note

#### Known bugs:
- Making too long quote, so that telegram splits it in two messages
- Using buttons from previous conversations may lead to unexpected results