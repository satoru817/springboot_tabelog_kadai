// ページロード時に日時と人数をローカルストレージから取得
//ブラウザのlocalStorageからのデータの取得
const DEFAULT_PEOPLE = 2;

const savedData = {
  savedDate: localStorage.getItem('reservationDate'),
  savedTime: localStorage.getItem('reservationTime'),
  savedPeople: localStorage.getItem('reservationPeople'),
};

const reservationFields = {
  time: document.getElementById('time'),
  date: document.getElementById('date'),
  people: document.getElementById('people'),
};

const restaurantId = document.getElementById('restaurantId').value;

const favBtn = document.getElementById('toggle-favorite');

const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const today = new Date();
const localDate = new Date(today.getTime() - today.getTimezoneOffset() * 60000).toISOString().split('T')[0];

//初期値をLocalStorageから読み込むメソッド
document.addEventListener('DOMContentLoaded', async function () {
  reservationFields.date.value =
    savedData.savedDate && savedData.savedDate >= localDate ? savedData.savedDate : localDate;
  reservationFields.date.min = localDate;

  //その日の始業、終業時刻をとってきて、timeFieldを作る
  await createTimeSelectOption();
  // reservationFields.date.addEventListener('change',createTimeSelectOption);

  // 人数の初期値を設定(localStorageに値がなければ2が入るようにしている)
  reservationFields.people.value = savedData.savedPeople || DEFAULT_PEOPLE;

  console.log('timeSelect.value:', reservationFields.time.value);

  await checkAvailability(
    restaurantId,
    reservationFields.date.value,
    reservationFields.time.value,
    reservationFields.people.value,
  );
});

async function createTimeSelectOption() {
  try {
    const openingHour = await getOpeningHour(reservationFields.date.value, restaurantId);

    const isBusinessDay = openingHour.isBusinessDay;
    const openingTime = openingHour.openingTime;
    const closingTime = openingHour.closingTime;

    if (!isBusinessDay) {
      reservationFields.time.innerHTML = '';
      const option = document.createElement('option');
      option.textContent = `Closed`;

      reservationFields.time.appendChild(option);
    } else {
      console.log('generateTimeOptionsINBusinessHoursは呼びだされています。');
      console.log(`reservationFields.time:${reservationFields.time.value}`);
      savedData.savedTime = reservationFields.time.value; //これがかけていた？
      generateTimeOptionsInBusinessHours(
        reservationFields.date.value,
        reservationFields.time,
        openingTime,
        closingTime,
      );
    }
  } catch (error) {
    console.error('エラーが発生しました:', error);
  }
}

//日時とレストランIdを利用してfetch通信を行いstartとfinish時刻をdbからとってくる
async function getOpeningHour(date, restaurantId) {
  try {
    const data = {
      restaurantId: restaurantId,
      date: date,
    };

    const response = await fetch('/restaurant/getOpeningHour', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    return result;
  } catch (error) {
    console.error('営業時間の取得にしっぱいしました :', error);
    throw error;
  }
}

// 日時と人数を選択すると予約可能か確認
document.getElementById('reservationForm').addEventListener('change', async function () {
  const date = document.getElementById('date').value;
  let time = document.getElementById('time').value;
  const people = document.getElementById('people').value;
  const restaurantId = document.getElementById('restaurantId').value;

  if (date && time && people) {
    // 選択した内容をローカルストレージに保存
    localStorage.setItem('reservationDate', date);
    localStorage.setItem('reservationTime', time);
    localStorage.setItem('reservationPeople', people);

    await createTimeSelectOption();

    time = document.getElementById('time').value;
    // 予約可能か確認（サーバー通信）
    await checkAvailability(restaurantId, date, time, people);
  }
});
function generateTimeOptionsInBusinessHours(selectedDate, selectElement, openTime, closeTime) {
  // 開始時刻と終了時刻を時間と分に分解
  var [openHour, openMinute] = openTime.split(':').map(Number);
  const [closeHour, closeMinute] = closeTime.split(':').map(Number);

  const now = new Date();
  var roundedMinutes = Math.ceil(now.getMinutes() / 15) * 15;
  if (roundedMinutes === 60) {
    now.setHours(now.getHours() + 1);
    roundedMinutes = 0;
  }

  // selectedDateが今日の日付かを確認
  const today = new Date();
  const isToday = selectedDate === localDate;
  console.log('selectedDate:', selectedDate);
  console.log('today:', today.toISOString().split('T')[0]);

  console.log('isToday:', isToday);

  if (isToday) {
    console.log('istodayでした');

    if (openHour < now.getHours()) {
      openHour = now.getHours();
      openMinute = roundedMinutes;
    } else if (openHour == now.getHours()) {
      openMinute = Math.max(openMinute, roundedMinutes);
    }
  }

  // 選択肢をクリア
  selectElement.innerHTML = '';

  // 15分間隔で選択肢を生成
  let selectedOptionFound = false;
  for (let hour = openHour; hour <= closeHour; hour++) {
    for (let minute = 0; minute < 60; minute += 15) {
      // 開始時刻より前、または終了時刻より後はスキップ
      if (hour === openHour && minute < openMinute) continue;
      if (hour === closeHour && minute > closeMinute) break;

      const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;

      const option = document.createElement('option');
      option.value = timeString;
      option.textContent = timeString;
      selectElement.appendChild(option);

      if (savedData.savedTime && savedData.savedTime === timeString && !selectedOptionFound) {
        console.log(`ifの中timeString:${timeString}`);
        option.selected = true; // 保存された時間が見つかったら選択する
        selectedOptionFound = true; // もう選択したのでフラグを立てる
        reservationFields.time.value = timeString; //これを忘れていた？
      }
    }
  }

  if (!selectedOptionFound) {
    console.log('falseでした');
    const option = reservationFields.time.options[0];
    reservationFields.time.value = option.value;
    option.selected = true;
  }

  console.log('reservationFields.time.value:::', reservationFields.time.value);
}

// 予約可能かどうかを確認する関数
async function checkAvailability(restaurantId, date, time, people) {
  console.log('checkAvailabilityは呼びだされています。');
  console.log('restaurantId', restaurantId);
  console.log('date', date);
  console.log('time', time);
  console.log('people', people);
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  const availabilityMessage = document.getElementById('availabilityMessage');

  const data = {
    restaurantId: restaurantId,
    date: date,
    time: time,
    people: people,
  };

  try {
    const response = await fetch('/restaurant/checkAvailability', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      console.log('response not OK');
      const errorMessage = await response.text();
      availabilityMessage.textContent = errorMessage;
      document.getElementById('confirmReservation').style.display = 'none';
      return;
    }

    const responseData = await response.text();
    availabilityMessage.textContent = responseData;
    document.getElementById('confirmReservation').style.display = 'block';
  } catch (error) {
    console.error('There was a problem with the fetch operation:', error);
  }
}

// 「本予約する」ボタンのクリックイベント
document.getElementById('confirmReservation').addEventListener('click', function () {
  const people = document.getElementById('people').value;
  const date = document.getElementById('date').value;
  const time = document.getElementById('time').value;

  const reservationDetails = `人数: ${people}名\n日付: ${date} \n 時間: ${time}`;
  document.getElementById('reservationDetails').innerText = reservationDetails;

  new bootstrap.Modal(document.getElementById('confirmReservationModal')).show();
});

//予約を確定するボタンのクリックイベント
document.getElementById('finalizeReservation').addEventListener('click', function () {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  const restaurantId = document.getElementById('restaurantId').value;
  const people = document.getElementById('people').value;
  const date = document.getElementById('date').value;
  const time = document.getElementById('time').value;
  const comment = document.getElementById('reservationComment').value;

  const reservationData = {
    restaurantId: restaurantId,
    people: people,
    date: date,
    time: time,
    comment: comment,
  };

  fetch('/restaurant/reservations', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken,
    },
    body: JSON.stringify(reservationData),
  })
    .then((response) => {
      if (response.ok) {
        alert('予約が確定しました！予約一覧画面で確認できます。');
        //モーダルを閉じる
        var modal = bootstrap.Modal.getInstance(document.getElementById('confirmReservationModal'));
        modal.hide();
      } else {
        alert('予約に失敗しました。再度お試し下さい。');
      }
    })
    .catch((error) => {
      console.error('There was a problem with the fetch operation:', error);
    });
});

favBtn.addEventListener('click', toggleFavorite);

async function toggleFavorite() {
  const restaurantId = favBtn.dataset.restaurantId;
  console.log('restaurantId:', restaurantId);
  const isFavorite = favBtn.dataset.isFavorite;
  console.log('isFavorite:', isFavorite);
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  const favoriteData = {
    isFavorite: isFavorite,
    restaurantId: restaurantId,
  };

  try {
    const response = await fetch('/favorite/api/toggle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(favoriteData),
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('Restaurant not found');
      } else if (response.status === 400) {
        throw new Error('Invalid request');
      } else {
        throw new Error('Server error occurred');
      }
    }

    const result = await response.json();
    console.log('result.isFavorite', result.isFavorite);
    if (result.isFavorite === true) {
      favBtn.innerHTML = '&#9829; Remove from Favorites';
    } else {
      favBtn.innerHTML = '&#9825; Add to Favorites';
    }

    favBtn.dataset.isFavorite = result.isFavorite;
  } catch (error) {
    console.error('Error:', error);
    alert(`failed to update your favorites:${error.message}`);
  }
}
