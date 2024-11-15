
const name = document.getElementById('name');
const password = document.getElementById('password');
const passwordConf = document.getElementById('passwordConfirmation');
const nameValidation = document.getElementById('name_validation');
const passwordValidation = document.getElementById('password_validation');
const minLengthValidation = document.getElementById('min_length_validation');
const email = document.getElementById('email');
const emailValidation = document.getElementById('email_validation');
const registrationForm = document.getElementById('registration_form');
const inputMistake = document.getElementById('input_mistakes');

// イベントリスナー
password.addEventListener('blur', validatePasswords);
password.addEventListener('blur', validatePasswordLength);
passwordConf.addEventListener('input', validatePasswords);
name.addEventListener('input', nameValidator);
email.addEventListener('blur', validateEmail);
registrationForm.addEventListener('submit', (e) => {
    // すべてのバリデーションを実行し、結果を取得
    const isValidEmail = validateEmail();
    const isValidName = nameValidator();
    const isValidPasswords = validatePasswords();
    const isValidPasswordLength = validatePasswordLength();

    // どれか一つでも無効なら、フォーム送信を中止
    if (!(isValidEmail && isValidName && isValidPasswords && isValidPasswordLength)) {
        e.preventDefault();
        inputMistake.style.display="block";
        console.log("event_prevented");
    }
});

// バリデーション関数
function validateEmail() {
    const regex = /^[\w-.]+@([\w-]+\.)+[a-zA-Z]{2,6}$/;
    const emailString = email.value;

    if (!regex.test(emailString)) {
        emailValidation.style.display = "block";
        return false;
    } else {
        emailValidation.style.display = "none";
        return true;
    }
}

function nameValidator() {
    if (name.value.includes('@')) {
        nameValidation.style.display = "block";
        return false;
    } else {
        nameValidation.style.display = "none";
        return true;
    }
}

function validatePasswords() {
    if (password.value !== passwordConf.value) {
        passwordValidation.style.display = "block";
        return false;
    } else {
        passwordValidation.style.display = "none";
        return true;
    }
}

function validatePasswordLength() {
    if (password.value.length < 8) {
        minLengthValidation.style.display = "block";
        return false;
    } else {
        minLengthValidation.style.display = "none";
        return true;
    }
}

document.addEventListener('DOMContentLoaded',function(){
    let croppie = new Croppie(document.getElementById('upload-demo'),{
        viewport:{  // 実際に切り取られる円形の領域の設定
            width:150,  // 幅150px
            height:150, // 高さ150px
            type:'circle' // 円形に切り取り
        },
        boundary:{  // 画像の移動や拡大縮小ができる領域の設定
            width:200,  // 幅200px
            height:200  // 高さ200px
        },
        enableExif: true  // スマホでの撮影時の画像の向きを自動補正
    });

    //ファイル選択時の処理
    document.getElementById('icon').addEventListener('change',function(e){
        if(this.files && this.files[0]){  // ファイルが選択されたか確認
            const reader = new FileReader();  // ファイルを読み込むための機能
            reader.onload = function(e){  // ファイル読み込み完了時の処理
                croppie.bind({  // 読み込んだ画像をCroppieに設定
                    url:e.target.result
                }).then(function(){
                   document.getElementById('crop-button').classList.remove('d-none');
                   // 切り取りボタンを表示
                });
            }
            reader.readAsDataURL(this.files[0]);  // 画像をBase64形式で読み込み
        }
    });

    document.getElementById('crop-button').addEventListener('click',function(){
        croppie.result({  // 切り取り結果を取得
            type: 'base64',    // Base64形式で出力
            size: 'viewport',  // viewport(円形)サイズで出力
            format:'jpeg',     // JPEG形式で出力
            quality: 0.9       // 品質90%で出力
        }).then(function(base64){  // 切り取り完了後の処理
            // 切り取った画像データをhidden inputにセット
            document.getElementById('icon-base64').value = base64;

            // プレビュー画像を表示
            document.getElementById('result-image').src = base64;
            document.getElementById('result-preview').classList.remove('d-none');

            alert('画像の切り取りが完了しました');
        })
    });
});
