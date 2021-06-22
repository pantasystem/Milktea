const express = require('express');
const app = express();

console.log('start server');

app.use(function(req, _, next){
    req.pipe(concat(function(data){
      req.body = data;
      next();
    }));
});

app.post('/webpushcallback', (req, res)=>{
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