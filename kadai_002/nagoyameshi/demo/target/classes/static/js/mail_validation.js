const mailInput = document.querySelector('input[id="email"]');
const emailValidation = document.getElementById('email_validation');
const emailValidationErrorAjax = document.getElementById('email_validate_error_ajax');
const emailValidateSuccess = document.getElementById('email_validate_success');


mailInput.addEventListener('input', mailValidator);

async function mailValidator() {
  const regex = /^[\w-.]+@([\w-]+\.)+[a-zA-Z]{2,6}$/;
  const emailString = mailInput.value;

  if (!regex.test(emailString)) {
    emailValidation.style.display = 'block';
    emailValidateSuccess.classList.add('d-none');
    return false;
  } else {
    emailValidation.style.display = 'none';
    await validateEmailAjax();
    return true;
  }
}

async function validateEmailAjax() {
  try {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const email = mailInput.value;
    console.log('validationが行われています:', email);

    const data = {
      email: email,
      userId: userId,
    };

    const response = await fetch('/auth/validateEmail', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`HTTP error!: ${response.status}`);
    }

    const isValid = await response.json();

    console.log('isValid:', isValid);

    if (!isValid) {
      emailValidationErrorAjax.classList.remove('d-none');
      emailValidateSuccess.classList.add('d-none');
      console.log('validation不合格');
    } else {
      emailValidationErrorAjax.classList.add('d-none');
      emailValidateSuccess.classList.remove('d-none');
      console.log('validation合格');
    }
  } catch (error) {
    console.error('名前のvalidationに失敗しました。');
    throw error;
  }
}
