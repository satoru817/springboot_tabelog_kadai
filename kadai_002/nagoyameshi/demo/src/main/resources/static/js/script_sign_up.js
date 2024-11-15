
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

let cropper;

function handleIconUpload(event){
    const file = event.target.files[0];
    if(file){
        const reader = new FileReader();
        reader.onload=function(e){
            //モーダル内の画像を設定
            const image = document.getElementById('cropImage');
            image.src = e.target.result;

            //モーダルを表示
            const modal = new bootstrap.Modal(document.getElementById('cropModal'));
            modal.show();

            if(cropper){
                cropper.destroy();
            }

            cropper = new Cropper(image,{
                aspectRatio: 1,
                viewMode: 1,
                dragMode: 'move',
                cropBoxResizable: true,
                cropBoxMovable: true,
                guides: true,
                center: true,
                highlight: false,
                background:true,
                autoCropArea: 0.8,
                responsive: true,
            });
        };
        reader.readAsDataURL(file);
    }
}

document.getElementById('cropButton').addEventListener('click',()=>{
    const canvas = cropper.getCroppedCanvas({
        width: 300,
        height: 300
    });

    //トリミングした画像をプレビュー表示
    const preview = document.getElementById('preview');
    preview.src = canvas.toDataURL();
    preview.classList.remove('d-none');
    document.querySelector('.icon-upload-box').classList.add('d-none');

    //トリミングした画像データをhidden input に設定
    document.getElementById('croppedImage').value = canvas.toDataURL();

    const modal = bootstrap.Modal.getInstance(document.getElementById('cropModal'));
    modal.hide();
});