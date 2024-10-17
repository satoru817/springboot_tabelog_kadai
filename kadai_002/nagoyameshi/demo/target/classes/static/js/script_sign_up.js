//TODO:nameに@が含まれているとき、及び、２つのpasswordが異なるときはjs側でもエラーを出し、submitできないようにする。
//TODO:パスワードが8文字以下の場合もエラーを出すようにしよう。メールのvalidationも先にやっとこう

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
