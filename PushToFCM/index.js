const express = require('express');
const app = express();
const Concat = require('concat-stream');
const fs = require('fs');
const i18n = require('i18n');
const crypto = require("crypto");


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

console.log("content-encoding", Buffer.from("Content-Encoding: aes128gcm\0\1"))
const publicKey = "BJgVD2cj1pNKNR2Ss3U_8e7P9AyoL5kWaxVio5aO16Cvnx-P1r7HH8SRb-h5tuxaydZ1ky3oO0V40s6t_uN1SdA"
const privateKey = "ciQ800G-6jyKWf6KKG94g5rCSU_l_rgbHbyHny_UsIM"
const authSecret = "43w_wOVYeF9XzyRyZL3O8g"
const body = "B8TZpK9vmRlCbkFTLG6l5gAAEABBBE8bArSb8EH1d8PH0J4Wrf/p2CY2rLUx55TgRayuyH0B3ZjJ2JiMDJH+c2FsA526yd08GVf7QjwqGWnNo4+LwpI4dyaI36CvBMPCf1lOAF50FV7JkgvMGyuYnzgUOh5KSvjDygDpygjRdFYI7amKXXCeRSMcwqn8lXgP1G6CUI43z88+bg/piRVBEnnALn2d60vzUzQNSXUdHAGCQ6aElewpxe3xT74ua0Bxcd4tB3fFaUzpOzYApQRpIlG5ITDTd1haMCuarE5vUGO+oPMsIv1nJO5keRhcvKBWSynj3d0+pGkgajrNjdQentooEQt5GmntKus+mUzLT+UQN1KNWnR3FN9LjsXX8fTk3Vhl7NabiW+N/vDPxI0/lw0VdBNeI460XcWi8aJd6Yb4THB0DjJ5p2JwXHB3zZ1dGSOA6f2hQWTQbVM/9Kisji0SEYdmysFNJaiajax2IeP8eG5lmJ/Lsq6Fs+CCRHnEUZIENEo0Mw3H44Wx/zWG1mEJxffIeuWUFbCcyfD8JYZkvilUu6azhPkTbhidbZ/NQO8oJHU21K5qQbOxiFhPD//cUORE/aCeF8uMdS1PgCvD1I0wfGOxmj3tLOXyvjmTNVzR7jZ7jRMro69/9K2dZ0YrcVMHgZuXfr9OtBD25FoHkkZ6uOxU8rX+FBDt4Af5A3mseX72dUFuhSCYh2akdrhUldvqnjb7IQKQpLrBDh4t4YGmPgXvVs/uSa48MOWNfgWUgng2BReDD7RwT1MeF3KQObAdOaocRZFRWx51JgDv16o7qr5Q/vd5TPIXGKloMgMMwCA5sAsIhmBdBZLjpPrFEspPVJvfGgcu7Hdb/9/xRAOsXBjlcg4iDetdTjkBpr5Gysn1EJfEZZAJaOTD4kgKtKURmynxvtX/nDKLmaIUt1f8Dfo/UKhq+9HcSfoohH/9AcFQCiSPPJrfJFt1BstJi/PhwbmWAMvxwbrzRZXzmXk8iiTEqbnlOqml+dnDZ4aJUjTrCb5OxbfaGsr3kXATcKT8jNKTOK6/0r25NNyGHCXlaF7awdjzQXLamWrsa/ieFmRnrpWKGobW45uNXEwiKmXvDWcVBH5PDeKQpu3IQSdzBXJSpYtrwpwnyyMbI0h+Y8JufjLWFZqNqCpcHG7CiwnKAR3icC3MLbZOS8oxo2AHYfhWXxzuqU5apbuCAzKNtk4N86wEuFLb1XkpI2AJ9u9k8MDBUdRxhxWhgKbQ2EOh4KZeKQDD6olVWR7ECU0otyM1g3+Ej89WOSrqJGhk+S704v1A73+EED0MFaY+MjrIOhCsrgL/Tcu0bbHYjOiuoEFN3Uf75Nr/+qTS6SatkHtqyIi24Y/NWCIZ976946gtym8iWi9aJieCLNYE4IuKjhgIOVrWqo57nbg0/4OQvTubSzBbis3x1X+PrL3IrSKufEs5pzEPuRbyhQfLHJ2gL4v2S0od5v9mxT3XacJAq2rHEd3/GXIxfgwWm/s/aSAIOnxU5DIM71UbVuEKBFtpk0IGt6DzsZiC7lELGUfR/VSv7Hg0d4aMmAsHPmo5r9FDDsALxp2OfL1XMg8/rPnUvRoKjdkY61MHfH7rQoXfy3Yidtx7dHeZ/O21tT/V83HXC6xNXpcGew0wyTjWGBqhVXkRZUjN3EteFniv90gg0SIPW4zzKJ42/uf0tzg76gZiJboAqWwlXmt+FmjnMg4990vA1obltewm6NIRha6EJb4fpafsnA8Hqk/rM7zu57vBLCr22o0VC7gery4xbRMW/es="
const key = webPushDecipher.buildReciverKey(publicKey, privateKey, authSecret);
    //console.log(`public_key:${PUBLIC_KEY}, private_key:${PRIVATE_KEY}, auth_secret:${AUTH_SECRET}`);
let decrypted = webPushDecipher.decrypt(body, key, true);
let receiver_curve = crypto.createECDH("prime256v1");
const keyId = "BE8bArSb8EH1d8PH0J4Wrf/p2CY2rLUx55TgRayuyH0B3ZjJ2JiMDJH+c2FsA526yd08GVf7QjwqGWnNo4+LwpI="

receiver_curve.setPrivateKey(Buffer.from(privateKey, "base64"));
const sharedSecret = receiver_curve.computeSecret(Buffer.from(keyId, "base64"));
console.log("generated pub key", receiver_curve.generateKeys())
console.log("sharedSecret", sharedSecret.toString("base64"))
console.log(Buffer.from("ciQ800G-6jyKWf6KKG94g5rCSU_l_rgbHbyHny_UsIM", "base64"))
console.log("hex", parseInt(Buffer.from([5, 10, 7]).toString("hex"), 16))

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
            accountId: accountId
        }
    };
    if(req.decodeJson.body.note != null) {
        message.data.noteId = req.decodeJson.body.note.id;
    }
    if(req.decodeJson.body.userId != null) {
        message.data.userId = req.decodeJson.body.userId;
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