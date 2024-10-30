// DOMContentLoaded時の初期化処理
document.addEventListener('DOMContentLoaded', function() {
    initializeAllForms();
});

// すべてのフォームを初期化
function initializeAllForms() {
    // 検索フォーム
    if (document.getElementById('searchForm')) {
        setupSearchForm();
        setupDatepickers();
        loadRecipes();
    }

    // 新規登録フォーム
    if (document.getElementById('newRecipeForm')) {
        initializeNewRecipeForm();
    }
}

// レシピ一覧画面：日付検索のフォームのセットアップ
function setupDatepickers() {
    $('.datepicker').datepicker({
        format: 'yyyy-mm-dd',
        language: 'en',
        autoclose: true,
        todayHighlight: true,
        clearBtn: true
    });
}

// レシピ一覧画面：検索フォームのセットアップ
function setupSearchForm() {
    const form = document.getElementById('searchForm');

    form.querySelector('button[type="reset"]').addEventListener('click', function() {
        setTimeout(() => {
            loadRecipes();
        }, 0);
    });

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(form);
        const params = new URLSearchParams();

        formData.forEach((value, key) => {
            if (value && value.trim() !== '') {
                // 全角スペース、半角スペース、複数のスペースを1つのスペースに正規化
                const normalizedValue = value.replace(/\s+/g, ' ').trim();

                if (key === 'favoriteRecipe') {
                    params.append(key, 'true');
                } else if (key === 'recipeNames' || key === 'ingredientNames') {
                    // スペース区切りの値を配列として扱う
                    const values = normalizedValue.split(' ');
                    values.forEach(v => params.append(key, v));
                } else {
                    params.append(key, value.trim());
                }
            }
        });
        loadRecipes(params);
    });
}

// レシピ一覧画面：レシピ一覧の読み込み
function loadRecipes(searchParams = new URLSearchParams()) {
    const container = document.getElementById('recipeContainer');
    container.innerHTML = '<div class="loading">読み込み中...</div>';

    fetch(`/recipes?${searchParams.toString()}`)
        .then(response => {
            if (!response.ok) throw new Error('レシピの取得に失敗しました');
            return response.json();
        })
        .then(recipeDetails => {
            displayRecipes(recipeDetails);
            updateRecipeCount(recipeDetails.length);
        })
        .catch(error => {
            console.error('Error:', error);
            container.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    ${error.message || 'レシピの取得に失敗しました。再度お試しください。'}
                </div>
            `;
        });
}

// レシピ一覧画面：レシピ一覧の表示
function displayRecipes(recipeDetails) {
    const container = document.getElementById('recipeContainer');
    container.innerHTML = '';

    recipeDetails.forEach(recipeDetail => {
        const recipe = recipeDetail.recipe;
        const col = document.createElement('div');
        col.className = 'col';

        const favoriteIcon = recipe.favorite ? '★' : '☆';
        const favoriteClass = recipe.favorite ? 'favorite-active' : 'favorite-inactive';

        col.innerHTML = `
            <div class="card h-100 position-relative">
            <button onclick="toggleFavorite(${recipe.id})"
                    class="favorite-button ${favoriteClass}"
                    data-id="${recipe.id}"
                    title="お気に入り切り替え">
                ${favoriteIcon}
            </button>
                <img src="${recipe.imagePath}" class="card-img-top" alt="${recipe.name}">
                <div class="card-actions">
                    <button onclick="location.href='/update.html'"
                            class="edit-button" title="編集">
                        ✎
                    </button>
                    <button onclick="confirmDelete(${recipe.id})"
                            class="delete-button" title="削除">
                        ×
                    </button>
                </div>
                <div class="card-body">
                    <h5 class="card-title">${recipe.name}</h5>
                    <p class="card-text">
                        <small class="text-muted">
                            作成日: ${formatDate(recipe.createdAt)}<br>
                            更新日: ${formatDate(recipe.updatedAt)}
                        </small>
                    </p>
                    <a href="/detail.html" class="btn btn-outline-primary">詳細</a>
                </div>
            </div>
        `;
        container.appendChild(col);
    });
}

// レシピ一覧画面：各レシピのお気に入り状態の切り替え
function toggleFavorite(recipeId) {
    const favoriteButton = document.querySelector(`.favorite-button[data-id='${recipeId}']`);
    const isCurrentlyFavorite = favoriteButton.classList.contains('favorite-active');
    const newFavorite = !isCurrentlyFavorite;

    favoriteButton.innerText = newFavorite ? '★' : '☆';
    favoriteButton.classList.toggle('favorite-active', newFavorite);
    favoriteButton.classList.toggle('favorite-inactive', !newFavorite);

    fetch(`/recipes/${recipeId}/favorite`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ favorite: newFavorite })
    })
    .then(response => {
        if (!response.ok) throw new Error('お気に入りの更新に失敗しました');
        return response.text();
    })
    .then(message => {
        console.log('Success:', message);
    })
    .catch(error => {
        console.error('Error:', error);
        alert('お気に入りの更新に失敗しました');
        // エラー時に元の状態に戻す
        favoriteButton.innerText = isCurrentlyFavorite ? '★' : '☆';
        favoriteButton.classList.toggle('favorite-active', isCurrentlyFavorite);
        favoriteButton.classList.toggle('favorite-inactive', !isCurrentlyFavorite);
    });
}

// LocalDateのフォーマット
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP');
}

// レシピ一覧画面：レシピの件数
function updateRecipeCount(count) {
    const countElement = document.getElementById('recipeCount');
    countElement.textContent = `全${count}件`;
}

// 削除モーダル
let deleteTargetId = null;
const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));

function confirmDelete(id) {
    deleteTargetId = id;
    deleteModal.show();
}

document.getElementById('confirmDelete').addEventListener('click', function() {
    if (deleteTargetId === null) return;

    fetch(`/recipes/${deleteTargetId}/delete`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) throw new Error('削除に失敗しました');
        return response.text();
    })
    .then(() => {
        deleteModal.hide();
        loadRecipes();
        showToast('レシピを削除しました');
    })
    .catch(error => {
        console.error('Error:', error);
        alert('削除に失敗しました。再度お試しください。');
    })
    .finally(() => {
        deleteTargetId = null;
    });
});

//　トースト設定
function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'position-fixed bottom-0 end-0 p-3';
    toast.style.zIndex = '5000';
    toast.innerHTML = `
        <div class="toast show" role="alert">
            <div class="toast-header">
                <strong class="me-auto">通知</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// レシピ新規作成画面：新規作成画面の初期化処理を追加
function initializeNewRecipeForm() {
    addInitialForms();
    setupFormButtons();
    setupFormSubmission();
}

// レシピ新規作成画面：初期フォームの追加
function addInitialForms() {
    const ingredientsContainer = document.getElementById('ingredientsContainer');
    if (ingredientsContainer && ingredientsContainer.children.length === 0) {
        addIngredientForm();
    }

    // 手順の初期フォームを追加
    const instructionsContainer = document.getElementById('instructionsContainer');
    if (instructionsContainer && instructionsContainer.children.length === 0) {
        addInstructionForm();
    }
}

// レシピ新規作成画面：材料・調理手順追加フォームボタンの設定
function setupFormButtons() {
    const addIngredientBtn = document.getElementById('addIngredient');
    const addInstructionBtn = document.getElementById('addInstruction');

    if (addIngredientBtn) {
        addIngredientBtn.addEventListener('click', function() {
            addIngredientForm();
        });
    }

    if (addInstructionBtn) {
        addInstructionBtn.addEventListener('click', function() {
            addInstructionForm();
        });
    }
}

// レシピ新規作成画面：フォーム送信の設定
function setupFormSubmission() {
    const form = document.getElementById('newRecipeForm');
    if (form) {
        form.removeAttribute('action');
        form.addEventListener('submit', submitNewRecipeForm);
    }
}

// レシピ新規作成画面：材料フォームの追加
function addIngredientForm() {
    const container = document.getElementById('ingredientsContainer');
    if (!container) {
        console.error('Ingredients container not found');
        return;
    }

    const ingredientHtml = createIngredientHtml();
    container.insertAdjacentHTML('beforeend', ingredientHtml);
    setupRemoveButtons();
}

// レシピ新規作成画面：手順フォームの追加
function addInstructionForm() {
    const container = document.getElementById('instructionsContainer');
    if (!container) {
        console.error('Instructions container not found');
        return;
    }

    const instructionHtml = createInstructionHtml();
    container.insertAdjacentHTML('beforeend', instructionHtml);
    setupRemoveButtons();
}

// レシピ新規作成画面：材料のHTML作成
function createIngredientHtml() {
    const ingredients = document.querySelectorAll('.ingredient');
    const nextIndex = ingredients.length + 1;

    return `
        <div class="ingredient mb-3 row">
            <div class="col-md-5">
                <input class="form-control" type="text" name="ingredient.name.${nextIndex}" placeholder="材料名" required/>
            </div>
            <div class="col-md-3">
                <input class="form-control" type="text" name="ingredient.quantity.${nextIndex}" placeholder="分量"/>
            </div>
            <div class="col-md-2">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="ingredient.arrange.${nextIndex}"/>
                    <label class="form-check-label">アレンジ</label>
                </div>
            </div>
            <div class="col-md-2">
                <button type="button" class="btn btn-danger btn-sm remove-ingredient">削除</button>
            </div>
        </div>
    `;
}

// レシピ新規作成画面：手順のHTML作成
function createInstructionHtml() {
    const instructions = document.querySelectorAll('.instruction');
    const nextIndex = instructions.length + 1;

    return `
        <div class="instruction mb-3 row">
            <div class="col-md-2">
                <input class="form-control" type="number" name="instruction.stepNumber.${nextIndex}"
                       value="${nextIndex}" readonly/>
            </div>
            <div class="col-md-6">
                <textarea class="form-control" name="instruction.content.${nextIndex}"
                         placeholder="手順内容" required></textarea>
            </div>
            <div class="col-md-2">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="instruction.arrange.${nextIndex}"/>
                    <label class="form-check-label">アレンジ</label>
                </div>
            </div>
            <div class="col-md-2">
                <button type="button" class="btn btn-danger btn-sm remove-instruction">削除</button>
            </div>
        </div>
    `;
}

// レシピ新規作成画面：削除ボタンのセットアップ
function setupRemoveButtons() {
    // 材料の削除ボタン
    document.querySelectorAll('.remove-ingredient').forEach(button => {
        button.removeEventListener('click', handleIngredientRemove);
        button.addEventListener('click', handleIngredientRemove);
    });
    // 手順の削除ボタン
    document.querySelectorAll('.remove-instruction').forEach(button => {
        button.removeEventListener('click', handleInstructionRemove);
        button.addEventListener('click', handleInstructionRemove);
    });
}

// レシピ新規作成画面：材料削除のハンドラー関数
function handleIngredientRemove() {
    const ingredients = document.querySelectorAll('.ingredient');
    if (ingredients.length > 1) {
        this.closest('.ingredient').remove();
    } else {
        alert('材料は最低1つ必要です');
    }
}

// レシピ新規作成画面：手順削除のハンドラー関数
function handleInstructionRemove() {
    const instructions = document.querySelectorAll('.instruction');
    if (instructions.length > 1) {
        this.closest('.instruction').remove();
        updateStepNumbers();
    } else {
        alert('手順は最低1つ必要です');
    }
}

// レシピ新規作成画面：手順番号の更新
function updateStepNumbers() {
    document.querySelectorAll('.instruction').forEach((instruction, index) => {
        const stepNumberInput = instruction.querySelector('input[name^="instruction.stepNumber"]');
        if (stepNumberInput) {
            stepNumberInput.value = index + 1;
            // name属性も更新
            stepNumberInput.name = `instruction.stepNumber.${index + 1}`;
        }
    });
}

// レシピ新規作成画面：新規レシピ詳細情報の登録
function submitNewRecipeForm(event) {
    event.preventDefault();

    if (!validateForm()) {
        return;
    }

    const formData = new FormData();
    // ファイル追加
    const fileInput = document.getElementById('imageFile');
    if (fileInput && fileInput.files.length > 0) {
        const file = fileInput.files[0];
        // ファイルサイズチェック (例: 5MB制限)
        if (file.size > 5 * 1024 * 1024) {
            alert('画像ファイルは5MB以下にしてください');
            return;
        }
        // MIMEタイプチェック
        if (!file.type.startsWith('image/')) {
            alert('画像ファイルのみアップロード可能です');
            return;
        }
        formData.append('file', file);
    }

    const recipeDetail = {
        recipe: {
            name: document.getElementById('name').value,
            recipeSource: document.getElementById('recipeSource').value,
            servings: document.getElementById('servings').value,
            remark: document.getElementById('remark').value,
            favorite: document.getElementById('favorite').checked
        },
        ingredients: getIngredients(),
        instructions: getInstructions()
    };

    // recipeDetailをBlob形式でformDataに追加
    formData.append('recipeDetail', JSON.stringify(recipeDetail));

    fetch('/recipes/new', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`サーバーエラー (${response.status}): ${text}`);
            });
        }
        return response.json();
    })
    .then(recipeDetail => {
        showToast('レシピを保存しました');
        window.location.href = '/recipes.html';
    })
    .catch(error => {
        console.error('Error:', error);
        alert('保存に失敗しました。再度お試しください。');
    });
}

// レシピ新規作成画面：必須項目のバリデーション
function validateForm() {
    const recipeName = document.getElementById('name').value;
    if (!recipeName) {
        alert('レシピ名は必須です');
        return false;
    }

    // 材料のチェック
    const ingredients = document.querySelectorAll('.ingredient');
    for (let i = 0; i < ingredients.length; i++) {
        const name = ingredients[i].querySelector('input[name^="ingredient.name"]').value;
        if (!name) {
            alert('材料名は必須です');
            return false;
        }
    }

    // 手順のチェック
    const instructions = document.querySelectorAll('.instruction');
    for (let i = 0; i < instructions.length; i++) {
        const content = instructions[i].querySelector('textarea[name^="instruction.content"]').value;
        if (!content) {
            alert('手順内容は必須です');
            return false;
        }
    }

    return true;
}

// レシピ新規作成画面：入力された材料情報の取得
function getIngredients() {
    const ingredients = [];
    const ingredientContainers = document.querySelectorAll('.ingredient');

    ingredientContainers.forEach(container => {
        ingredients.push({
            name: container.querySelector('input[name^="ingredient.name"]').value,
            quantity: container.querySelector('input[name^="ingredient.quantity"]').value,
            arrange: container.querySelector('input[name^="ingredient.arrange"]').checked,
        });
    });

    return ingredients;
}

// レシピ新規作成画面：入力された調理手順の取得
function getInstructions() {
    const instructions = [];
    const instructionContainers = document.querySelectorAll('.instruction');

    instructionContainers.forEach(container => {
        instructions.push({
            stepNumber: container.querySelector('input[name^="instruction.stepNumber"]').value || (index + 1).toString(),
            content: container.querySelector('textarea[name^="instruction.content"]').value,
            arrange: container.querySelector('input[name^="instruction.arrange"]').checked,
        });
    });

    return instructions;
}

// TODO:画像をアップデート機能、デフォルトでNO＿DATAが表示されるようにする機能。
// TODO:バリデーションエラーはサーバーサイドからJSONのエラーメッセージを受け取り、それをフロントエンドのフォームに表示させたい。