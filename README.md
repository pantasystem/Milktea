# Milktea
MisskeyのAndroidクライアント

## Description
これはMisskeyのAndroidクライアントリリース版はまだない

## How to build

以下のクラスを
app/src/java/jp/panta/misskeyandroidclient直下に作成する
AppSecretはインスタンスのドメイン/devで作成することができます
```
package jp.panta.misskeyandroidclient;

import java.util.LinkedHashMap;

import jp.panta.misskeyandroidclient.model.auth.Instance;

public abstract class SecretConstant {
    
    public static LinkedHashMap<String, Instance> getInstances(){
        LinkedHashMap<String, Instance> map = new LinkedHashMap<String, Instance>();
        
        //導入するインスタンスをmapにputしていく
        //domain　例: https://misskey.io, host: misskey.io
        map.put("https://misskey.io", new Instance("domain", "host","AppSecret"));
        
        return map;
    }
}
```