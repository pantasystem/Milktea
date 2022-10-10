package main

import (
	"fmt"

	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
)

func main() {

	fmt.Printf("アカウント登録を開始します")
	fmt.Printf("Emailアドレスを入力してください")
	var email string
	fmt.Scan(&email)

	fmt.Printf("パスワードを入力してください")
	var password string
	fmt.Scan(&password)

	dao := dao.Init()

	fmt.Printf("以下内容で登録します。\n")
	fmt.Printf("Email:%s, Password:%s", email, password)
	account := domain.AdAccount{
		Email: email,
	}
	account.SetPassword(password)
	result, err := dao.NewAdAccountRepository().Create(&account)
	if err != nil {
		fmt.Printf("登録に失敗しました")
	} else {
		fmt.Printf("登録に成功しました ID:%s", result.Id)
	}

}
