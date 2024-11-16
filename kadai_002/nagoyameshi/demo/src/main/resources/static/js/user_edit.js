const nameInput = document.querySelector('input[id="name"]');
const mailInput = document.querySelector('input[id="email"]');

const nameValidation = document.getElementById('name_validation');
const validationSuccess = document.getElementById('successValidate');

document.addEventListener('DOMContentLoaded',async function(){
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
            document.getElementById('confirm_image').classList.remove('d-none');

            alert('画像の切り取りが完了しました');
        })
    });

    nameInput.addEventListener('input', await nameValidator);
    mailInput.addEventListener('input',await mailValidator);



});

async function nameValidator() {
    if (nameInput.value.includes('@')) {
        nameValidation.style.display = "block";
        validationSuccess.classList.add("d-none");
        return false;
    } else {
        validationSuccess.classList.remove('d-none');
        nameValidation.style.display = "none";
        await validateName();
        return true;
    }
}


async function validateName(){
    try{
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const name = nameInput.value;
        console.log('validationが行われています:',name);

        const data = {
            name: name,
        }

        const response = await fetch('/auth/validateName',{
            method: 'POST',
            headers: {
                'Content-Type':'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        });

        if(!response.ok){
            throw new Error(`HTTP error!: ${response.status}`)
        }

        const isValid = await response.json();

        console.log('isValid:',isValid);

        if(!isValid){
           document.getElementById('validateError').classList.remove('d-none');
           validationSuccess.classList.add('d-none');
           console.log('validation不合格');
        }else{
           document.getElementById('validateError').classList.add('d-none');
           console.log('validation合格');
        }

    }catch(error){
        console.error("名前のvalidationに失敗しました。");
        throw error;
    }
}