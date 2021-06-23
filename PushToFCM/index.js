const express = require('express');
const app = express();
const Concat = require('concat-stream');
const fs = require('fs');

const AUTH_SECRET = fs.readFileSync('./key/auth_secret.txt');
const PUBLICK_KEY = fs.readFileSync('./key/public_key.txt', 'utf8');
const PRIVATE_KEY = fs.readFileSync('./key/private_key.txt', 'utf8');

console.log('start server');


const rawBodyMiddlware = (req, _, next) => {
    req.pipe(new Concat(function(data) {
        req.rawBody = data;
        next();
    }))
}

const decodeBodyMiddleware = (req, res, next) => {
    let rawBody = req.rawBody;

    next();
}

app.post('/webpushcallback', rawBodyMiddlware, decodeBodyMiddleware ,(req, res)=>{
    console.log();
    let deviceToken = req.query.deviceToken
    let accountId = req.query.accountId;
    if(!(deviceToken && accountId)) {
        return cres.status(422).end();
    }
    console.log(`rawBody:${req.rawBody}`);
    console.log(`deviceToken:${deviceToken}, accountId:${accountId}`);
    res.json({status: 'ok'});
});
app.listen(3000);