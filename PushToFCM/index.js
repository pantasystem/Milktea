const express = require('express');
const app = express();
const Concat = require('concat-stream');

console.log('start server');

/*app.use(function(req, _, next){
    req.pipe(concat(function(data){
      req.body = data;
      next();
    }));
});*/

const rawBodyMiddlware = (req, _, next) => {
    req.pipe(new Concat(function(data) {
        req.rawBody = data;
        next();
    }))
}

const decodeBodyMiddleware = (req, res, next) => {
    console.log('decodeBody');
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