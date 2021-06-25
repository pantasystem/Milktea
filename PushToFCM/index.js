const express = require('express');
const app = express();
const Concat = require('concat-stream');
const fs = require('fs');
const i18n = require('i18n');
const notificationBuilder = require('./notification_builder');

const webPushDecipher = require('./webPushDecipher.js');

const AUTH_SECRET = fs.readFileSync('./key/auth_secret.txt', 'utf8');
const PUBLICK_KEY = fs.readFileSync('./key/public_key.txt', 'utf8');
const PRIVATE_KEY = fs.readFileSync('./key/private_key.txt', 'utf8');

const admin = require('firebase-admin');
admin.initializeApp({
    credential: admin.credential.applicationDefault(),
});
const messaging = admin.messaging();

i18n.configure({
    locales: ['ja', 'en'],
    defaultLocal: 'en',
    directory: __dirname + "/locales",
    objectNotation: true
});


console.log('start server');

app.use(i18n.init);

const switchLangMiddleware = (req, _, next) => {
    if(req.query.lang) {
        i18n.setLocale(req, req.query.lang);
    }else{
        i18n.setLocale(req, 'en');
    }
    next();
}

const rawBodyMiddlware = (req, _, next) => {
    req.pipe(new Concat(function(data) {
        req.rawBody = data;
        next();
    }))
}

const decodeBodyMiddleware = (req, res, next) => {
    let rawBody = req.rawBody;
    if (!rawBody) { 
        return res.status(200).send('Invalid Body.').end(); 
    }
    const converted = rawBody.toString('base64');
    const key = webPushDecipher.buildReciverKey(PUBLICK_KEY, PRIVATE_KEY, AUTH_SECRET);
    let decrypted = webPushDecipher.decrypt(converted, key, false);
    req.rawJson = decrypted;
    next();
}

const parseJsonMiddleware = (req, res, next) => {
    try {
        req.decodeJson = JSON.parse(req.rawJson);
        next();
    }catch(e) {
        console.log('parse error', req.rawJson, e);
        return res.status(400).end();
    }
}

app.post('/webpushcallback', rawBodyMiddlware, decodeBodyMiddleware, parseJsonMiddleware, switchLangMiddleware ,(req, res)=>{
    let deviceToken = req.query.deviceToken
    let accountId = req.query.accountId;
    if(!(deviceToken && accountId)) {
        return res.status(410).end();
    }

    if(req.decodeJson.type != 'notification') {
        return;
    }
    let convertedNotification = notificationBuilder.generateNotification(res, req.decodeJson.body);


    const message = {
        token: deviceToken,
        notification: {
            title: convertedNotification.title,
            body: convertedNotification.body
        },
        data: {
            title: convertedNotification.title,
            body: convertedNotification.body,
            type: convertedNotification.type,
            notificationId: req.decodeJson.body.id,
            accountId: accountId,
        }
    };
    if(req.decodeJson.body.noteId != null) {
        message.data.noteId = req.decodeJson.body.noteId;
    }
    if(req.decodeJson.body.userId != null) {
        message.data.userId = req.decodeJson.body.userId;
    }
    console.log(message);
    
    messaging.send(message).then((res)=>{
        console.log(`send:${res}`);
    }).catch((e)=>{
        console.error('message送信失敗', e);
    });

    res.json({status: 'ok'});
});

app.get('/test', switchLangMiddleware,(req, res)=>{
    let msg= res.__('test.message');
    res.json({'msg': msg});
})
app.listen(3000);