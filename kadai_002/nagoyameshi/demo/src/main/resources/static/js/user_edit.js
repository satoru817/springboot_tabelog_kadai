const nameInput = document.querySelector('input[id="name"]');
const mailInput = document.querySelector('input[id="email"]');
const imageInput = document.getElementById('imageInput');

const icon = document.getElementById('icon');

const nameValidation = document.getElementById('name_validation');
const emailValidation = document.getElementById('email_validation');
const emailValidationErrorAjax = document.getElementById('email_validate_error_ajax');
const emailValidateSuccess = document.getElementById('email_validate_success');
const validationSuccess = document.getElementById('successValidate');

const cancelImage = document.getElementById('cancelImage');
const selectImageBtn = document.getElementById('selectImageBtn');
const deleteImageBtn = document.getElementById('deleteImageBtn');
const cropButton = document.getElementById('cropButton');
const cancelCropBtn = document.getElementById('cancelCropBtn');
const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
const editCancelBtn = document.getElementById('editCancelBtn');

const croppieElement = document.getElementById('croppieElement');
const croppieContainer = document.getElementById('croppieContainer');
//Croppieインスタンスの設定
let croppie = null;

const alertMessage = document.getElementById('icon_replace_alert');
const resultImage = document.getElementById('result-image');
const resultPreview = document.getElementById('result-preview');


document.addEventListener('DOMContentLoaded',async function(){





    //画像関連の要素
    nameInput.addEventListener('input', await nameValidator);
    mailInput.addEventListener('input',await mailValidator);

    //画像選択ボタンをクリックするとinput type="file"の要素が開く
    selectImageBtn.addEventListener('click',function(){
        console.log('selectImageBtnは押されました。');
        imageInput.click();
    })



    //選択された画像をcroppieと結びつける
    imageInput.addEventListener('change',function(e){
        if(e.target.files && e.target.files[0]){
            const reader = new FileReader();
            reader.onload = function(e){
                if(!croppie){
                    initCroppie();
                }
                croppieContainer.classList.remove('d-none');
                croppie.bind({
                    url: e.target.result
                });
            };
            reader.readAsDataURL(e.target.files[0]);
        }
    });

    //切り取り確定ボタンのイベントリスナー
    cropButton.addEventListener('click',async function() {

        const base64 = await croppie.result({
            type:'base64',
            size:'viewport',
            format:'jpeg',
            quality:0.9
        });

        icon.value=base64;

        // プレビュー画像を表示
        resultImage.src = base64;
        cancelImage.src = base64;
        resultPreview.classList.remove('d-none');
        alertMessage.classList.remove('d-none');
        const editModal = bootstrap.Modal.getInstance(document.getElementById('imageEditModal'));
        editModal.hide();

        alert('画像の切り取りが終わりました');


    });

    deleteImageBtn.addEventListener('click',function(){
        console.log('deleteImageBtnは押されています');
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
        deleteModal.show();
    });

    confirmDeleteBtn.addEventListener('click',async function(){
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        console.log("confirmDeleteBtnは押されました。");
        try{
            const response = await fetch('/api/profile/image',{
                method: 'DELETE',
                headers: {
                    [csrfHeader]:csrfToken
                }
            });

            if(response.ok){
                document.querySelector('.profile-image').src='/images/default_profile.png';
                document.querySelector('.current-profile-image').src='/images/default_profile.png';

                //モーダルを閉じる
                const deleteModal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
                deleteModal.hide();
                const editModal = bootstrap.Modal.getInstance(document.getElementById('imageEditModal'));
                editModal.hide();
            }
        }catch(error){
            console.error('Error deleting image:',error);
        }
    });

    cancelCropBtn.addEventListener('click',function(){
        croppieContainer.classList.add('d-none');
        if(croppie){
            croppie.destroy();
            croppie = null;
        }
    });

    editCancelBtn.addEventListener('click',function(){
        icon.value=null;//フォームで送られる値をnullに直す
        resultPreview.classList.add('d-none');
        const editCancelModal = bootstrap.Modal.getInstance(document.getElementById('cancelEditModal'));
        editCancelModal.hide();
        alertMessage.classList.add('d-none');
    });

});




function initCroppie(){
    console.log("initCroppieは呼ばれました");
    croppie = new Croppie(croppieElement,{
        viewport:{width:200,height:200,type:'circle'},
        boundary:{width:300,height:300},
        enableOrientation:true
    });
}

async function mailValidator() {
    const regex = /^[\w-.]+@([\w-]+\.)+[a-zA-Z]{2,6}$/;
    const emailString = mailInput.value;

    if (!regex.test(emailString)) {
        emailValidation.style.display = "block";
        emailValidateSuccess.classList.add('d-none');
        return false;
    } else {
        emailValidation.style.display = "none";
        await validateEmailAjax();
        return true;
    }
}

async function validateEmailAjax(){
    try{
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const email = mailInput.value;
        console.log('validationが行われています:',email);

        const data = {
            email: email,
        }

        const response = await fetch('/auth/validateEmail',{
            method: 'POST',
            headers: {
                'Content-Type':'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        });

        if(!response.ok){
            throw new Error(`HTTP error!: ${response.status}`)
        }

        const isValid = await response.json();

        console.log('isValid:',isValid);

        if(!isValid){
           emailValidationErrorAjax.classList.remove('d-none');
           emailValidateSuccess.classList.add('d-none');
           console.log('validation不合格');
        }else{
           emailValidationErrorAjax.classList.add('d-none');
           emailValidateSuccess.classList.remove('d-none');
           console.log('validation合格');
        }

    }catch(error){
        console.error("名前のvalidationに失敗しました。");
        throw error;
    }
}

async function nameValidator() {
    if (nameInput.value.includes('@')) {
        nameValidation.style.display = "block";
        validationSuccess.classList.add("d-none");
        return false;
    } else {
        validationSuccess.classList.remove('d-none');
        nameValidation.style.display = "none";
        await validateNameAjax();
        return true;
    }
}


async function validateNameAjax(){
    try{
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const name = nameInput.value;
        console.log('validationが行われています:',name);

        const data = {
            name: name,
        }

        const response = await fetch('/auth/validateName',{
            method: 'POST',
            headers: {
                'Content-Type':'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        });

        if(!response.ok){
            throw new Error(`HTTP error!: ${response.status}`)
        }

        const isValid = await response.json();

        console.log('isValid:',isValid);

        if(!isValid){
           document.getElementById('validateError').classList.remove('d-none');
           validationSuccess.classList.add('d-none');
           console.log('validation不合格');
        }else{
           document.getElementById('validateError').classList.add('d-none');
           console.log('validation合格');
        }

    }catch(error){
        console.error("名前のvalidationに失敗しました。");
        throw error;
    }
}