document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form');
  form.addEventListener('submit', function (event) {
    let valid = true;
    const errorMessages = [];

    // 会社名のバリデーション
    const name = document.getElementById('name').value;
    if (name.trim() === '') {
      valid = false;
      errorMessages.push('会社名を入力してください。');
    }

    // 郵便番号のバリデーション
    const postalCode = document.getElementById('postal_code').value;
    const postalCodePattern = /^\d{3}-\d{4}$/; // 例: 123-4567
    if (!postalCodePattern.test(postalCode)) {
      valid = false;
      errorMessages.push('郵便番号は「xxx-xxxx」の形式で入力してください。');
    }

    // 住所のバリデーション
    const address = document.getElementById('address').value;
    if (address.trim() === '') {
      valid = false;
      errorMessages.push('住所を入力してください。');
    }

    // 電話番号のバリデーション
    const phoneNumber = document.getElementById('phone_number').value;
    const phonePattern = /^(0\d{1,4}-\d{1,4}-\d{4}|\d{10}|\d{11})$/; // 市外局番付き、10桁または11桁の電話番号を許可
    if (!phonePattern.test(phoneNumber)) {
      valid = false;
      errorMessages.push(
        '電話番号は「0xx-xxxx-xxxx」または「xxxxxxxxxx」または「xxxxxxxxxxx」の形式で入力してください。',
      );
    }

    // 説明のバリデーション
    const description = document.getElementById('description').value;
    if (description.trim() === '') {
      valid = false;
      errorMessages.push('会社についての説明を入力してください。');
    }

    // エラーメッセージの表示
    if (!valid) {
      event.preventDefault(); // フォーム送信を防止
      alert(errorMessages.join('\n'));
    }
  });
});
