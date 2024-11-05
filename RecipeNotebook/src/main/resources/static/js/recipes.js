// DOMContentLoaded時の初期化処理
document.addEventListener('DOMContentLoaded', function() {
    initializeModal();
    initializeAllViews();
});

// モーダルの初期化
function initializeModal() {
    const modalElement = document.getElementById('deleteModal');
    if (modalElement) {
        window.deleteModal = new bootstrap.Modal(modalElement);

        // 確認ボタンのイベントリスナーを設定
        document.getElementById('confirmDelete')?.addEventListener('click', function() {
            if (window.deleteTargetId === null) return;

            fetch(`/api/recipes/${window.deleteTargetId}/delete`, {
                method: 'DELETE'
            })
            .then(response => {
                if (!response.ok) throw new Error('削除に失敗しました');
                return response.text();
            })
            .then(() => {
                window.deleteModal.hide();

                const　currentPath = window.location.pathname;
                if (currentPath.includes('detail') || currentPath.includes('update')) {
                    window.location.href = '/recipes';
                } else {
                    loadRecipes();
                }
            })
            .then(() => {
                showToast('レシピを削除しました');
            })
            .catch(error => {
                console.error('Error:', error);
                alert('削除に失敗しました。再度お試しください。');
            })
            .finally(() => {
                window.deleteTargetId = null;
            });
        });
    }
}

// 削除の確認
function confirmDelete(id) {
    window.deleteTargetId = id;
    window.deleteModal.show();
}

// 画面の初期化
function initializeAllViews() {
    // 検索・レシピ一覧画面
    if (document.getElementById('searchForm')) {
        setupSearchForm();
        setupDatepickers();
        loadRecipes();
    }

    // レシピ詳細画面(recipeIdはdetail.html内で定義)
    if (document.getElementById('displayRecipeDetail')) {
        const path = window.location.pathname;
        const recipeId = path.split("/")[2];
        loadRecipeDetail(recipeId);
        navigateToEdit(recipeId);
    }

    // レシピ新規作成・編集画面
    if (document.getElementById('recipeForm')) {
        initializeRecipeForm();

        const path = window.location.pathname;
        if(path.includes('/update')) {
            const recipeId = path.split("/")[2];
            loadRecipeDetail(recipeId);
            navigateToRecipeDetail(recipeId);
        }
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

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        clearValidationErrors()
        const formData = new FormData(form);
        const params = new URLSearchParams();

        formData.forEach((value, key) => {
            if (value && value.trim() !== '') {
                // 全角スペース、半角スペース、複数のスペースを1つのスペースに正規化
                const normalizedValue = value.replace(/\s+/g, ' ').trim();

                if (key === 'favoriteRecipe') {
                    params.append(key, 'true');
                }

                if (key === 'recipeNames' || key === 'ingredientNames') {
                    // スペース区切りの値を配列として扱う
                    const values = normalizedValue.split(' ');
                    values.forEach(v => params.append(key, v));
                }

                if (key !== 'favoriteRecipe' && key !== 'recipeNames' && key !== 'ingredientNames') {
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

    fetch(`/api/recipes?${searchParams.toString()}`)
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    handleValidationErrors(data.errors);
                    throw new Error(data.message);
                });
            }
            return response.json();
        })
        .then(recipeDetails => {
            displayRecipes(recipeDetails);
            updateRecipeCount(recipeDetails.length);
        })
        .catch(errorMessage => {
            console.error(errorMessage);
            container.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    不正な検索条件が指定されています。
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
                <div class="card-img-top" >
                  <img src="${recipe.imagePath}" class="img-fit-contain" alt="${recipe.name}">
                </div>
                <div class="card-actions">
                    <button onclick="location.href='/recipes/${recipe.id}/update'"
                            class="edit-button" title="編集">
                        ✎
                    </button>
                    <button onclick="window.confirmDelete(${recipe.id})"
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
                    <button onclick="location.href='/recipes/${recipe.id}/detail'"
                            class="btn btn-outline-primary" title="詳細">
                        詳細
                    </button>
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

    fetch(`/api/recipes/${recipeId}/favorite`, {
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
    setTimeout(() => toast.remove(), 5000);
}

// レシピ詳細情報の読み込み
function loadRecipeDetail(recipeId) {
    fetch(`/api/recipes/${recipeId}`)
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message);
                });
            }
            return response.json();
        })
        .then(recipeDetail => {
            displayRecipe(recipeDetail);
            displayPreFilledRecipe(recipeDetail);
        })
        .catch(errorMessage => {
            console.error(errorMessage);
            alert('レシピの取得に失敗しました。再度お試しください')
        });
}

// レシピ詳細画面：レシピ詳細画面の表示
function displayRecipe(recipeDetail) {
    const container = document.getElementById('displayRecipeDetail');

    if (!container) return;

    container.innerHTML = '';

    const recipe = recipeDetail.recipe;
    const ingredients = recipeDetail.ingredients;
    const instructions = recipeDetail.instructions;

    const favoriteIcon = recipe.favorite ? '★' : '☆';
    const favoriteClass = recipe.favorite ? 'favorite-active' : 'favorite-inactive';

    container.innerHTML = `
        <button onclick="toggleFavorite(${recipe.id})"
                class="favorite-button ${favoriteClass}"
                data-id="${recipe.id}"
                title="お気に入り切り替え">
            ${favoriteIcon}
        </button>
        <div class="recipe-header text-center">
            <h1 class="display-5 mb-4">${recipe.name || ''}</h1>
            <div class="row justify-content-center mb-4">
                <div class="col-md-8">
                    <img src="${recipe.imagePath || ''}"
                         class="recipe-image img-fluid"
                         alt="${recipe.name}">
                </div>
            </div>
        </div>

        <div class="recipe-info">
            <div class="row align-items-center mb-4">
                <div class="col-sm-6">
                    <h5 class="text-muted mb-1">情報元</h5>
                    <p class="mb-0">${recipe.recipeSource || '記載なし'}</p>
                </div>
                <div class="col-sm-6">
                    <h5 class="text-muted mb-1">分量</h5>
                    <p class="mb-0">${recipe.servings || '記載なし'}</p>
                </div>
            </div>
        </div>

        <div class="recipe-info">
            <h4 class="mb-3">材料</h4>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 35%">材料</th>
                            <th style="width: 35%">分量</th>
                            <th style="width: 30%">アレンジ</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${ingredients.map(ingredient => `
                            <tr>
                                <td>${ingredient.name || ''}</td>
                                <td>${ingredient.quantity || ''}</td>
                                <td>
                                    <div class="form-check">
                                        <input class="form-check-input"
                                               type="checkbox"
                                               ${ingredient.arrange ? 'checked' : ''}
                                               disabled>
                                        <label class="form-check-label">
                                            ${ingredient.arrange ? 'アレンジあり' : 'アレンジなし'}
                                        </label>
                                    </div>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        </div>

        <div class="recipe-info">
            <h4 class="mb-3">調理手順</h4>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 15%">手順</th>
                            <th style="width: 55%">内容</th>
                            <th style="width: 30%">アレンジ</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${instructions.map(instruction => `
                            <tr>
                                <td>${instruction.stepNumber || ''}</td>
                                <td>${instruction.content || ''}</td>
                                <td>
                                    <div class="form-check">
                                        <input class="form-check-input"
                                               type="checkbox"
                                               ${instruction.arrange ? 'checked' : ''}
                                               disabled>
                                        <label class="form-check-label">
                                            ${instruction.arrange ? 'アレンジあり' : 'アレンジなし'}
                                        </label>
                                    </div>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        </div>

        ${recipe.remark ? `
            <div class="recipe-info">
                <h4 class="mb-3">備考</h4>
                <p class="mb-0">${recipe.remark}</p>
            </div>
        ` : ''}

        <div class="recipe-meta ms-3">
            <p class="mb-1">作成日：${formatDate(recipe.createdAt)}</p>
            <p class="mb-0">更新日：${formatDate(recipe.updatedAt)}</p>
        </div>
    `;

    const deleteButton = document.querySelector('.btn-outline-danger');
    if (deleteButton) {
        deleteButton.onclick = () => window.confirmDelete(recipe.id);
    }
}

// レシピ新規作成・編集画面：フォームの初期化処理を追加
function initializeRecipeForm() {
    addInitialForms();
    setupFormButtons();
    setupFormSubmission();
}

// レシピ新規作成・編集画面：材料・調理手順フォームの追加
function addInitialForms() {
    const ingredientsContainer = document.getElementById('ingredientsContainer');
    if (ingredientsContainer && ingredientsContainer.children.length === 0) {
        addIngredientForm();
    }

    const instructionsContainer = document.getElementById('instructionsContainer');
    if (instructionsContainer && instructionsContainer.children.length === 0) {
        addInstructionForm();
    }
}

// レシピ新規作成・編集画面：材料・調理手順追加フォームボタンの設定
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

// レシピ新規作成・編集画面：フォーム送信の設定
function setupFormSubmission() {
    const form = document.getElementById('recipeForm');
    if (form) {
        // ブラウザのデフォルトのフォームバリデーションを無効化（バックエンドでのバリデーションを行うため）
        form.setAttribute('novalidate', 'true');
        form.removeAttribute('action');
        form.addEventListener('submit', submitRecipeForm);
    }
}

// レシピ新規作成・編集画面：材料フォームの追加
function addIngredientForm() {
    const container = document.getElementById('ingredientsContainer');
    const ingredientHtml = createIngredientHtml();
    container.insertAdjacentHTML('beforeend', ingredientHtml);
    setupRemoveButtons();
}

// レシピ新規作成・編集画面：手順フォームの追加
function addInstructionForm() {
    const container = document.getElementById('instructionsContainer');
    const instructionHtml = createInstructionHtml();
    container.insertAdjacentHTML('beforeend', instructionHtml);
    setupRemoveButtons();
}

// レシピ新規作成・編集画面：材料のHTML作成
function createIngredientHtml() {
    const ingredients = document.querySelectorAll('.ingredient');
    const nextIndex = ingredients.length + 1;

    return `
        <div class="ingredient mb-3 row">
            <div class="col-md-5">
                <input class="form-control" type="text" name="ingredient.name.${nextIndex}" placeholder="材料名（必須）"/>
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

// レシピ新規作成・編集画面：手順のHTML作成
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
                         placeholder="手順内容（必須）"></textarea>
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

// レシピ新規作成・編集画面：削除ボタンのセットアップ
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

// レシピ新規作成・編集画面：材料削除のハンドラー関数
function handleIngredientRemove() {
    const ingredients = document.querySelectorAll('.ingredient');
    if (ingredients.length > 1) {
        this.closest('.ingredient').remove();
    } else {
        alert('材料は最低1つ必要です');
    }
}

// レシピ新規作成・編集画面：手順削除のハンドラー関数
function handleInstructionRemove() {
    const instructions = document.querySelectorAll('.instruction');
    if (instructions.length > 1) {
        this.closest('.instruction').remove();
        updateStepNumbers();
    } else {
        alert('手順は最低1つ必要です');
    }
}

// レシピ新規作成・編集画面：手順番号の更新
function updateStepNumbers() {
    document.querySelectorAll('.instruction').forEach((instruction, index) => {
        const stepNumberInput = instruction.querySelector('input[name^="instruction.stepNumber"]');
        if (stepNumberInput) {
            stepNumberInput.value = index + 1;
            stepNumberInput.name = `instruction.stepNumber.${index + 1}`;
        }
    });
}

// レシピ新規作成・編集画面：フォーム情報の送信
async function submitRecipeForm(event) {
    event.preventDefault();

    const fileInput = document.getElementById('imageFile');
    let base64Image = null;

    if (fileInput && fileInput.files.length > 0) {
        base64Image = await convertToBase64(fileInput.files[0]);
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
        instructions: getInstructions(),
    };

    const recipeDetailWithImageData = {
        recipeDetail: recipeDetail,
        imageData: base64Image
    }

    fetch('/api/recipes/new', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(recipeDetailWithImageData)
    })
    .then(response => response.json().then(responseJson => {
        if (!response.ok) { // 200番台以外はエラーハンドリング
            if (responseJson.message && responseJson.message.includes('バリデーション')) {
                handleValidationErrors(responseJson.errors);
                throw new Error(responseJson.message);
            } else if (responseJson.message &&
                      (responseJson.message.includes('ファイルのサイズ') ||
                       responseJson.message.includes('画像ファイルのみ'))) {
                throw new Error(responseJson.message);
            } else {
                throw new Error('予期しないエラーが発生しました');
            }
        }
        return responseJson;
    }))
    .then(recipeDetail => {
        const recipeId = recipeDetail.recipe.id;
        window.location.href = `/recipes/${recipeId}/detail`;
        showToast('レシピを保存しました');
    })
    .catch(errorMessage => {
        alert(errorMessage);
    });
}

// レシピ新規作成・編集画面：ファイルをBase64に変換する関数
async function convertToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}

// バリデーションエラー発生時のハンドリング
function handleValidationErrors(errors) {
    errors.forEach(error => {
        let convertedField;
        if (error.field === 'recipeDetail.recipe.name') {
            convertedField = 'recipe.name';
        } else if (/^recipeDetail\.ingredients\[\d+\]\.name$/.test(error.field)) {
            const index = parseInt(error.field.match(/\[(\d+)\]/)[1], 10) + 1;
            convertedField = `ingredient.name.${index}`;
        } else if (/^recipeDetail\.instructions\[\d+\]\.content$/.test(error.field)) {
            const index = parseInt(error.field.match(/\[(\d+)\]/)[1], 10) + 1;
            convertedField = `instruction.content.${index}`;
        } else {
            convertedField = error.field;
        }
        displayValidationError(convertedField, error.message);
    });
}

// 既存のバリデーションエラーの削除
function clearValidationErrors() {
    document.querySelectorAll('[data-error-for]').forEach(element => element.remove());
    document.querySelectorAll('.is-invalid').forEach(element => {
        element.classList.remove('is-invalid');
    });
}

// バリデーションエラーの表示
function displayValidationError(fieldName, message) {
    const existingError = document.querySelector(`[data-error-for="${fieldName}"]`);
    if (existingError) {
        existingError.remove();
    }

    const formField = document.querySelector(`[name="${fieldName}"]`);
    if (formField) {
      const errorElement = document.createElement('div');
      errorElement.className = 'text-danger mt-1';
      errorElement.setAttribute('data-error-for', fieldName);
      errorElement.textContent = message;

      formField.parentNode.insertBefore(errorElement, formField.nextSibling);
      formField.classList.add('is-invalid');
      formField.addEventListener('input', function() {
          errorElement.remove();
          formField.classList.remove('is-invalid');
      });
    }
}

// レシピ新規作成・編集画面：入力された材料情報の取得
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

// レシピ新規作成・編集画面：入力された調理手順の取得
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

function displayPreFilledRecipe(recipeDetail) {
    const container = document.getElementById('recipeForm');

    if (!container) return;

    document.getElementById('recipeId').value = recipeDetail.recipe.id;
    document.getElementById('name').value = recipeDetail.recipe.name;
    document.getElementById('recipeSource').value = recipeDetail.recipe.recipeSource;
    document.getElementById('servings').value = recipeDetail.recipe.servings;
    document.getElementById('remark').value = recipeDetail.recipe.remark;
    document.getElementById('favorite').checked = recipeDetail.recipe.favorite;

    recipeDetail.ingredients.forEach((ingredient, index) => {
        if (index < recipeDetail.ingredients.length - 1) {
            addIngredientForm();
        }
        const ingredientInputs = document.querySelectorAll(`.ingredient:nth-child(${index + 1}) input, .ingredient:nth-child(${index + 1}) textarea`);
        ingredientInputs[0].value = ingredient.name;
        ingredientInputs[1].value = ingredient.quantity;
        ingredientInputs[2].checked = ingredient.arrange;
    });

    recipeDetail.instructions.forEach((instruction, index) => {
        if (index < recipeDetail.instructions.length - 1) {
            addInstructionForm();
        }
        const instructionInputs = document.querySelectorAll(`.instruction:nth-child(${index + 1}) input, .instruction:nth-child(${index + 1}) textarea`);
        instructionInputs[0].value = instruction.stepNumber;
        instructionInputs[1].value = instruction.content;
        instructionInputs[2].checked = instruction.arrange;
    });
}

// レシピ詳細情報へのリダイレクト
function navigateToRecipeDetail(recipeId) {
    document.getElementById('toRecipeDetail').addEventListener('click', () => {
        window.location.href = `/recipes/${recipeId}/detail`;
    });
}

// レシピ編集画面へのリダイレクト
function navigateToEdit(recipeId) {
    document.getElementById('toEdit').addEventListener('click', () => {
        window.location.href = `/recipes/${recipeId}/update`;
    });
}

//TODO:画像ファイルの事前入力、updateとnewのfetchを分岐（updateはまだ設定していない）、updateページの「詳細画面に戻る」が効かない