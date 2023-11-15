const express = require('express');
const app = express();
const Concat = require('concat-stream');
const fs = require('fs');
const i18n = require('i18n');
const notificationBuilder = require('./notification_builder');

const webPushDecipher = require('./webPushDecipher.js');

const AUTH_SECRET = fs.readFileSync('./key/auth_secret.txt', 'utf8');
const PUBLIC_KEY = fs.readFileSync('./key/public_key.txt', 'utf8');
const PRIVATE_KEY = fs.readFileSync('./key/private_key.txt', 'utf8');

const admin = require('firebase-admin');
const { resourceUsage } = require('process');
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
        console.log('Invalid Body');
        return res.status(200).send('Invalid Body.').end(); 
    }
    const converted = rawBody.toString('base64');
    const key = webPushDecipher.buildReciverKey(PUBLIC_KEY, PRIVATE_KEY, AUTH_SECRET);
    //console.log(`public_key:${PUBLIC_KEY}, private_key:${PRIVATE_KEY}, auth_secret:${AUTH_SECRET}`);
    try {
        let decrypted = webPushDecipher.decrypt(converted, key, false);
        req.rawJson = decrypted;
    } catch (e) {
        console.log(`Decrypt Error: ${e}, request original url: ${req.originalUrl}, headers:${JSON.stringify(req.headers)}`);
        throw e;
    }
    
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

app.post('/webpushcallback', rawBodyMiddlware, decodeBodyMiddleware, parseJsonMiddleware, switchLangMiddleware ,async (req, res, next)=>{
    let deviceToken = req.query.deviceToken;
    let accountId = req.query.accountId;
    console.log('call webpushcallback');
    if(!(deviceToken && accountId)) {
        console.warn('無効な値');
        return res.status(410).end();
    }

    if(req.decodeJson.type != 'notification') {
        return res.status(500).end();
    }
    let convertedNotification;
    try {
        convertedNotification = notificationBuilder.generateNotification(res, req.decodeJson.body);
    } catch (e) {
        return res.status(500).end();
    }


    const msgData = {
        title: convertedNotification.title,
        body: convertedNotification.body,
        type: convertedNotification.type,
        notificationId: req.decodeJson.body.id,
        accountId: accountId
    }
    
    const message = {
        token: deviceToken,
        notification: {
            title: convertedNotification.title,
            body: convertedNotification.body
        }
    };
    if(req.decodeJson.body.note != null) {
        msgData.noteId = req.decodeJson.body.note.id;
    }
    if(req.decodeJson.body.userId != null) {
        msgData.userId = req.decodeJson.body.userId;
    }
    message.data = msgData;
    // console.log(message);
    try {
        await messaging.send(message);
        res.status(204).end();
        return;
    } catch (e) {
        if (
            [
              'The registration token is not a valid FCM registration token',
              'Requested entity was not found.',
              'NotRegistered.'
            ].includes(e.message)
        ) {
            console.log('トークン切れ');
            res.status(410).end();
        } else {
            console.error("未知のエラー", e);
            res.status(500).end();
        }
        return;
    }

});

app.post("/webpushcallback-4-mastodon", rawBodyMiddlware, decodeBodyMiddleware, parseJsonMiddleware, switchLangMiddleware ,async (req, res)=>{
    let deviceToken = req.query.deviceToken;
    let accountId = req.query.accountId;
    if(!(deviceToken && accountId)) {
        console.warn('無効な値');
        return res.status(410).end();
    }
    const title = req.decodeJson.title;
    const body = req.decodeJson.body;
    const message = {
        token: deviceToken,
        notification: {
            title: title,
            body: body
        }
    };
    message.data = {
        title: title,
        body: body,
        type: req.decodeJson.notification_type,
        notificationId: req.decodeJson.notification_id,
        accountId: accountId
    }
    console.log(message);
    try {
        await messaging.send(message);
        res.status(204).end();
        return;
    } catch (e) {
        if (
            [
              'The registration token is not a valid FCM registration token',
              'Requested entity was not found.',
              'NotRegistered.'
            ].includes(e.message)
        ) {
            console.log('トークン切れ');
            res.status(410).end();
        } else {
            console.error("未知のエラー", e);
            res.status(500).end();
        }
        return;
    }
});

app.get('/health', switchLangMiddleware,(req, res)=>{
    let msg= res.__('test.message');
    res.json({'msg': msg});
})
app.listen(3000);