// ページロード時に日時と人数をローカルストレージから取得
//TODO:条件分岐を加えて下さい。
//TODO:今日が選択されたとき現在時刻より後の時刻が表示されるようにしてください。
document.addEventListener('DOMContentLoaded', function() {
  const savedDate = localStorage.getItem('reservationDate');
  const savedTime = localStorage.getItem('reservationTime');
  const savedPeople = localStorage.getItem('reservationPeople');

  const restaurantId = document.getElementById('restaurantId').value;

  // 日付の初期値を現在の日付に設定
  const dateField = document.getElementById('date');
  //dateField.value = savedDate || new Date().toISOString().split('T')[0];
  dateField.min = new Date().toISOString().split('T')[0];

  // 時間の初期値を現在時刻に設定（15分刻み）
  const timeSelect = document.getElementById('time');
  const now = new Date();
  const roundedMinutes = Math.ceil(now.getMinutes() / 15) * 15;
  if(roundedMinutes===60){
    now.setHours(now.getHours()+1);
    roundedMinutes = 0;
  }

    const option = document.createElement('option');
    option.textContent = `${now.getHours().toString().padStart(2, '0')}:${roundedMinutes.toString().padStart(2, '0')}`;
    timeSelect.appendChild(option);



  //その日の始業、終業時刻をとってきて、timeFieldを作る
  dateField.addEventListener('change',async function(){
  try{
    console.log("try節の中に入っています。");
    const openingHour = await getOpeningHour(dateField.value,restaurantId);
    console.log("openingHour:",openingHour);
    const isBusinessDay = openingHour.isBusinessDay;
    const openingTime = openingHour.openingTime;
    const closingTime = openingHour.closingTime;
    console.log("isBusinessDay:",isBusinessDay);
    console.log("openingTime:",openingTime);
    if(!isBusinessDay){
        timeSelect.innerHTML = '';
        const option = document.createElement('option');
        option.textContent = `休業日です。`;
        timeSelect.appendChild(option);
    }else{
        generateTimeOptionsInBusinessHours(timeSelect,openingTime,closingTime);
    }
  }catch(error){
    console.error("エラーが発生しました:",error);
  }
  });

  // 人数の初期値を設定
  if (savedPeople) document.getElementById('people').value = savedPeople;

  // 保存された値があれば選択
  if (savedTime) timeSelect.value = savedTime;
});

//日時とレストランIdを利用してfetch通信を行いstartとfinish時刻をdbからとってくる
async function getOpeningHour(date,restaurantId){
    try{
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const data = {
            restaurantId: restaurantId,
            date: date,
        };

        const response = await fetch('/restaurant/getOpeningHour',{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        });

        if(!response.ok){
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();

        return result;
    }catch(error){
        console.error('営業時間の取得にしっぱいしました :',error);
        throw error;
    }
}

// 日時と人数を選択すると予約可能か確認
document.getElementById('reservationForm').addEventListener('change', function() {
  const date = document.getElementById('date').value;
  const time = document.getElementById('time').value;
  const people = document.getElementById('people').value;
  const restaurantId = document.getElementById('restaurantId').value;

  if (date && time && people) {
      // 選択した内容をローカルストレージに保存
      localStorage.setItem('reservationDate', date);
      localStorage.setItem('reservationTime', time);
      localStorage.setItem('reservationPeople', people);

      // 予約可能か確認（サーバー通信）
      checkAvailability(restaurantId,date, time, people);
  }
});

function generateTimeOptionsInBusinessHours(selectElement, openTime, closeTime) {
    // 開始時刻と終了時刻を時間と分に分解
    const [openHour, openMinute] = openTime.split(':').map(Number);
    const [closeHour, closeMinute] = closeTime.split(':').map(Number);

    // 選択肢をクリア
    selectElement.innerHTML = '';

    // 15分間隔で選択肢を生成
    for (let hour = openHour; hour <= closeHour; hour++) {
        for (let minute = 0; minute < 60; minute += 15) {
            // 開始時刻より前、または終了時刻より後はスキップ
            if (hour === openHour && minute < openMinute) continue;
            if (hour === closeHour && minute > closeMinute) continue;

            const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;

            const option = document.createElement('option');
            option.value = timeString;
            option.textContent = timeString;
            selectElement.appendChild(option);
        }
    }
}

// 予約可能かどうかを確認する関数
function checkAvailability(restaurantId , date, time, people) {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  // ここでサーバー通信で予約可否を確認する処理を追加
  const data = {
        restaurantId: restaurantId,
        date : date,
        time : time,
        people : people,
  };

  fetch('/restaurant/checkAvailability',{
       method: 'POST',
       headers:{
            'Content-Type':'application/json',
            [csrfHeader]: csrfToken
       },
       body: JSON.stringify(data)
  })
  .then(response=>{
        if(!response.ok){
            return response.text().then(errorMessage =>{
                document.getElementById('availabilityMessage').textContent=errorMessage;
                document.getElementById('confirmReservation').style.display='none';
            });
        }
        return response.text();
  })
  .then(data=>{
        document.getElementById('availabilityMessage').textContent=data;
        document.getElementById('confirmReservation').style.display='block';
  })
  .catch(error=>{
        console.error('There was a problem with the fetch operation:',error);
  });


}

// 「本予約する」ボタンのクリックイベント
document.getElementById("confirmReservation").addEventListener("click",function(){
    const people = document.getElementById("people").value;
    const date = document.getElementById("date").value;
    const time = document.getElementById("time").value;

    const reservationDetails = `人数: ${people}名\n日付: ${date} \n 時間: ${time}`;
    document.getElementById("reservationDetails").innerText = reservationDetails;

    new bootstrap.Modal(document.getElementById('confirmReservationModal')).show();
});

//予約を確定するボタンのクリックイベント
document.getElementById("finalizeReservation").addEventListener("click",function(){
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    const restaurantId = document.getElementById("restaurantId").value;
    const people = document.getElementById("people").value;
    const date = document.getElementById("date").value;
    const time = document.getElementById("time").value;
    const comment = document.getElementById("reservationComment").value;

    const reservationData = {
        restaurantId: restaurantId,
        people: people,
        date: date,
        time: time,
        comment: comment
    };

    fetch('/restaurant/reservations',{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body:JSON.stringify(reservationData)
    })
    .then(response=>{
        if(response.ok){
            alert("予約が確定しました！予約一覧画面で確認できます。");
            //モーダルを閉じる
            var modal = bootstrap.Modal.getInstance(document.getElementById('confirmReservationModal'));
            modal.hide();
        }else{
            alert("予約に失敗しました。再度お試し下さい。");
        }
    })
    .catch(error=>{
        console.error('There was a problem with the fetch operation:',error);
    });
})

