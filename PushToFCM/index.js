const express = require('express');
const app = express();
const Concat = require('concat-stream');
const fs = require('fs');

const webPushDecipher = require('./webPushDecipher.js');

const AUTH_SECRET = fs.readFileSync('./key/auth_secret.txt', 'utf8');
const PUBLICK_KEY = fs.readFileSync('./key/public_key.txt', 'utf8');
const PRIVATE_KEY = fs.readFileSync('./key/private_key.txt', 'utf8');
const SERVER_KEY = fs.readFileSync('./key/serverkey.txt', 'utf8');

const admin = require('firebase-admin');
admin.initializeApp({
    credential: admin.credential.applicationDefault(),
});
const messaging = admin.messaging();

console.log('start server');


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
    let decrypted = webPushDecipher.decrypt(converted, key, true);
    req.rawJson = decrypted;
    next();
}

const parseJsonMiddleware = (req, res, next) => {
    try {
        req.json = JSON.parse(req.rawJson);
        next();
    }catch(e) {
        console.log('parse error', req.rawJson, e);
        return res.status(400).end();
    }
}

app.post('/webpushcallback', rawBodyMiddlware, decodeBodyMiddleware, parseJsonMiddleware ,(req, res)=>{
    let deviceToken = req.query.deviceToken
    let accountId = req.query.accountId;
    if(!(deviceToken && accountId)) {
        return res.status(410).end();
    }

    messaging.sendToDevice(deviceToken, JSON.stringify(
        {
            accountId: accountId,
            data: req.json
        }
    )).then((res)=>{
        console.log('送信成功');
    }).catch((e)=>{
        console.error('fcm送信失敗:', e);
    });

    res.json({status: 'ok'});
});
app.listen(3000);