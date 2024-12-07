//サーバーサイドでもvalidationは行っているが、フロントでも行う。
const textValidationPatterns = {
  romanOnly: /^[a-zA-Z\p{White_Space}]+$/u,
  katakanaOnly: /^[\p{Script=Katakana}\p{White_Space}]+$/u,
};

const errorMessageDisplay = {
  blank: document.querySelector('#nameForReservationBlankError'),
  other: document.querySelector('#nameForReservationValidationError'),
};

const inputForm = document.querySelector('#nameForReservation');

inputForm.addEventListener('input', reservationNameValidator);

function reservationNameValidator() {
  const inputName = inputForm.value;

  Object.values(errorMessageDisplay).forEach((elem) => {
    elem.classList.add('d-none');
  });

  if (nullCheck(inputName)) {
    errorMessageDisplay.blank.classList.remove('d-none');
  } else if (romanValidator(inputName) && katakanaValidator(inputName)) {
    errorMessageDisplay.other.classList.remove('d-none');
  }
}

function romanValidator(text) {
  return !textValidationPatterns.romanOnly.test(text);
}

function katakanaValidator(text) {
  return !textValidationPatterns.katakanaOnly.test(text);
}

function nullCheck(text) {
  return text.trim() === '';
}
