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
        req.body = data;
    }))
}

app.post('/webpushcallback', rawBodyMiddlware ,(req, res)=>{
    console.log();
    let deviceToken = req.query.deviceToken
    let accountId = req.query.accountId;
    if(!(deviceToken && accountId)) {
        return cres.status(422).end();
    }
    console.log(`deviceToken:${deviceToken}, accountId:${accountId}`);
    res.json({status: 'ok'});
});
app.listen(3000);