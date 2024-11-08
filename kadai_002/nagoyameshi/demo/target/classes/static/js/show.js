// ページロード時に日時と人数をローカルストレージから取得
//TODO:条件分岐を加えて下さい。
//TODO:今日が選択されたときの時間の選択肢に関する問題を解決してください。
document.addEventListener('DOMContentLoaded', function() {
  const savedDate = localStorage.getItem('reservationDate');
  const savedTime = localStorage.getItem('reservationTime');
  const savedPeople = localStorage.getItem('reservationPeople');

  // 日付の初期値を現在の日付に設定
  const dateField = document.getElementById('date');
  dateField.value = savedDate || new Date().toISOString().split('T')[0];
  dateField.min = new Date().toISOString().split('T')[0];

  // 時間の初期値を現在時刻に設定（15分刻み）
  const timeField = document.getElementById('time');
  const now = new Date();
  const roundedMinutes = Math.ceil(now.getMinutes() / 15) * 15;
  timeField.value = savedTime || `${now.getHours().toString().padStart(2, '0')}:${roundedMinutes.toString().padStart(2, '0')}`;

  // 時刻の選択肢を15分ごとに生成
  const timeSelect = document.getElementById('time');
  generateTimeOptions(timeSelect);



  // 人数の初期値を設定
  if (savedPeople) document.getElementById('people').value = savedPeople;

  // 保存された値があれば選択
  if (savedTime) timeSelect.value = savedTime;
});

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

      // 予約可能か確認（サーバー通信を模擬）
      checkAvailability(restaurantId,date, time, people);
  }
});

// 時刻の選択肢を15分ごとに生成する関数
function generateTimeOptions(selectElement) {
  for (let hour = 0; hour < 24; hour++) {
      for (let minute = 0; minute < 60; minute += 15) {
          const option = document.createElement('option');
          option.value = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
          option.textContent = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
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

