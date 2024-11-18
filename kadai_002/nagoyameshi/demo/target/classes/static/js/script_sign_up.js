// DOM要素の取得
const elements = {
    form: document.getElementById('registration_form'),
    name: document.getElementById('name'),
    email: document.getElementById('email'),
    password: document.getElementById('password'),
    passwordConf: document.getElementById('passwordConfirmation'),
    icon: document.getElementById('icon'),
    uploadDemo: document.getElementById('upload-demo'),
    cropButton: document.getElementById('crop-button'),
    iconBase64: document.getElementById('icon-base64'),
    resultImage: document.getElementById('result-image'),
    resultPreview: document.getElementById('result-preview')
};

// バリデーションメッセージ要素
const messages = {
    nameValidation: document.getElementById('name_validation'),
    emailValidation: document.getElementById('email_validation'),
    emailValidationErrorAjax: document.getElementById('email_validate_error_ajax'),
    emailValidateSuccess: document.getElementById('email_validate_success'),
    passwordValidation: document.getElementById('password_validation'),
    minLengthValidation: document.getElementById('min_length_validation'),
    inputMistake: document.getElementById('input_mistakes'),
    validateError: document.getElementById('validateError'),
    validationSuccess: document.getElementById('successValidate')
};

// バリデーションの設定
const validationConfig = {
    passwordMinLength: 8,
    emailRegex: /^[\w-.]+@([\w-]+\.)+[a-zA-Z]{2,6}$/
};

// CSRF設定の取得
const csrf = {
    token: document.querySelector('meta[name="_csrf"]')?.getAttribute('content'),
    header: document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content')
};

// バリデーション関数
const validators = {
    // パスワードの一致確認
    validatePasswords() {
        const isValid = elements.password.value === elements.passwordConf.value;
        messages.passwordValidation.style.display = isValid ? "none" : "block";
        return isValid;
    },

    // パスワード長のバリデーション
    validatePasswordLength() {
        const isValid = elements.password.value.length >= validationConfig.passwordMinLength;
        messages.minLengthValidation.style.display = isValid ? "none" : "block";
        return isValid;
    },

    // メールアドレスのバリデーション
    async validateEmail() {
        const emailString = elements.email.value;
        const isValidFormat = validationConfig.emailRegex.test(emailString);

        messages.emailValidation.style.display = isValidFormat ? "none" : "block";
        messages.emailValidateSuccess.classList.toggle('d-none', !isValidFormat);

        if (!isValidFormat) return false;

        try {
            const isValid = await validateEmailAjax();
            messages.emailValidationErrorAjax.classList.toggle('d-none', isValid);
            messages.emailValidateSuccess.classList.toggle('d-none', !isValid);
            return isValid;
        } catch (error) {
            console.error("Email validation failed:", error);
            return false;
        }
    },

    // ユーザー名のバリデーション
    async validateName() {
        const nameString = elements.name.value;
        const isValidFormat = !nameString.includes('@');

        messages.nameValidation.style.display = isValidFormat ? "none" : "block";
        messages.validationSuccess.classList.toggle('d-none', !isValidFormat);

        if (!isValidFormat) return false;

        try {
            const isValid = await validateNameAjax();
            messages.validateError.classList.toggle('d-none', isValid);
            messages.validationSuccess.classList.toggle('d-none', !isValid);
            return isValid;
        } catch (error) {
            console.error("Name validation failed:", error);
            return false;
        }
    }
};

// Ajax検証関数
async function validateEmailAjax() {
    try {
        const response = await fetch('/auth/validateEmail', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token
            },
            body: JSON.stringify({ email: elements.email.value })
        });

        if (!response.ok) throw new Error(`HTTP error: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("Email validation request failed:", error);
        throw error;
    }
}

async function validateNameAjax() {
    console.log(elements.name.value);
    const data = {
        name: elements.name.value,
    }
    try {
        const response = await fetch('/auth/validateName', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error(`HTTP error: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("Name validation request failed:", error);
        throw error;
    }
}

// イベントリスナーの設定
function setupEventListeners() {
    // フォーム送信のバリデーション
    elements.form.addEventListener('submit', async (e) => {
        e.preventDefault();

        try {
            const validations = await Promise.all([
                validators.validateEmail(),
                validators.validateName(),
                validators.validatePasswords(),
                validators.validatePasswordLength()
            ]);

            if (validations.every(Boolean)) {
                elements.form.submit();
            } else {
                messages.inputMistake.style.display = "block";
            }
        } catch (error) {
            console.error("Form validation failed:", error);
            messages.inputMistake.style.display = "block";
        }
    });

    // 入力フィールドのバリデーション
    elements.password.addEventListener('blur', () => {
        validators.validatePasswords();
        validators.validatePasswordLength();
    });
    elements.passwordConf.addEventListener('input', validators.validatePasswords);
    elements.email.addEventListener('blur', validators.validateEmail);
    elements.name.addEventListener('input', validators.validateName);
}

// 画像クロップ機能の設定
function setupImageCropper() {
    const croppie = new Croppie(elements.uploadDemo, {
        viewport: {
            width: 150,
            height: 150,
            type: 'circle'
        },
        boundary: {
            width: 200,
            height: 200
        },
        enableExif: true
    });

    elements.icon.addEventListener('change', function(e) {
        if (this.files?.[0]) {
            const reader = new FileReader();
            reader.onload = (e) => {
                croppie.bind({
                    url: e.target.result
                }).then(() => {
                    elements.cropButton.classList.remove('d-none');
                });
            };
            reader.readAsDataURL(this.files[0]);
        }
    });

    elements.cropButton.addEventListener('click', () => {
        croppie.result({
            type: 'base64',
            size: 'viewport',
            format: 'jpeg',
            quality: 0.9
        }).then(base64 => {
            elements.iconBase64.value = base64;
            elements.resultImage.src = base64;
            elements.resultPreview.classList.remove('d-none');
            alert('画像の切り取りが完了しました');
        });
    });
}

// 初期化
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    setupImageCropper();
});