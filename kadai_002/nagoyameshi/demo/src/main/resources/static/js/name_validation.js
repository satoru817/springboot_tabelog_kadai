const nameInput = document.querySelector('input[id="name"]');
const nameValidation = document.getElementById('name_validation');
const validationSuccess = document.getElementById('successValidate');
const nonBlank = document.getElementById('name_exist_validation');
const userId = document.getElementById('userId')?.value ?? '';

nameInput.addEventListener('input', nameValidator);

async function nameValidator() {
  if (nameInput.value.includes('@')) {
    nameValidation.style.display = 'block';
    validationSuccess.classList.add('d-none');
    return false;
  } else if (nameInput.value.trim() === '') {
    validationSuccess.classList.add('d-none');
    nonBlank.classList.remove('d-none');
  } else {
    validationSuccess.classList.remove('d-none');
    nameValidation.style.display = 'none';
    nonBlank.classList.add('d-none');
    await validateNameAjax();
    return true;
  }
}

async function validateNameAjax() {
  try {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const name = nameInput.value;
    console.log('validationが行われています:', name);

    const data = {
      name: name,
      userId: userId,
    };

    const response = await fetch(`/auth/validateName`, {
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
      document.getElementById('validateError').classList.remove('d-none');
      validationSuccess.classList.add('d-none');
      console.log('validation不合格');
    } else {
      document.getElementById('validateError').classList.add('d-none');
      console.log('validation合格');
    }
  } catch (error) {
    console.error('名前のvalidationに失敗しました。');
    console.log('error:', error);
    throw error;
  }
}
