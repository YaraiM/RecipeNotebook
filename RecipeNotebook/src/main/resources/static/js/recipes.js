document.addEventListener('DOMContentLoaded', function() {
    loadRecipes();
    setupSearchForm();
    setupDatepickers();
    setupIngredientButton();
    setupInstructionButton();
});

function setupDatepickers() {
    $('.datepicker').datepicker({
        format: 'yyyy-mm-dd',
        language: 'ja',
        autoclose: true,
        todayHighlight: true,
        clearBtn: true
    });
}

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
                    <button onclick="location.href='/recipes/${recipe.id}/update'"
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
                    <a href="/recipes/${recipe.id}" class="btn btn-outline-primary">詳細</a>
                </div>
            </div>
        `;
        container.appendChild(col);
    });
}

function toggleFavorite(recipeId) {
    const favoriteButton = document.querySelector(`.favorite-button[data-id='${recipeId}']`);
    const isCurrentlyFavorite = favoriteButton.classList.contains('favorite-active');
    const newFavorite = !isCurrentlyFavorite;

    console.log(`Recipe ID: ${recipeId}, New Favorite: ${newFavorite}`);  // デバッグ

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

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP');
}

function updateRecipeCount(count) {
    const countElement = document.getElementById('recipeCount');
    countElement.textContent = `全${count}件`;
}

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

function submitRecipeForm(event) {
    event.preventDefault();

    const formData = new FormData(document.getElementById('recipeForm'));
    const recipeDetail = {
        recipe: {
            name: formData.get('recipe.name'),
            imagePath: formData.get('recipe.imagePath'),
            recipeSource: formData.get('recipe.recipeSource'),
            servings: formData.get('recipe.servings'),
            remark: formData.get('recipe.remark'),
            favorite: formData.get('recipe.favorite') ? true : false,
        },
        ingredients: getIngredients(),
        instructions: getInstructions(),
    };

    const method = formData.get('id') ? 'PUT' : 'POST';
    const url = formData.get('id')
        ? `/recipes/${formData.get('id')}/update`
        : '/recipes/new';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(recipeDetail)
    })
    .then(response => {
        if (!response.ok) throw new Error('保存に失敗しました');
        return response.json();
    })
    .then(recipe => {
        showToast('レシピを保存しました');
        // フォームをリセットする
        document.getElementById('recipeForm').reset();
        loadRecipes(); // 新しいレシピリストを読み込む
    })
    .catch(error => {
        console.error('Error:', error);
        alert('保存に失敗しました。再度お試しください。');
    });
}

function getIngredients() {
    const ingredients = [];
    const ingredientContainers = document.querySelectorAll('.ingredient');

    ingredientContainers.forEach(container => {
        const name = container.querySelector('input[name="name"]').value;
        const quantity = container.querySelector('input[name="quantity"]').value;
        ingredients.push({ name, quantity });
    });

    return ingredients;
}

function getInstructions() {
    const instructions = [];
    const instructionContainers = document.querySelectorAll('.instruction');

    instructionContainers.forEach(container => {
        const content = container.querySelector('textarea[name="content"]').value;
        instructions.push({ content });
    });

    return instructions;
}

function setupIngredientButton() {
    document.getElementById('addIngredient').addEventListener('click', function() {
        const container = document.getElementById('ingredientsContainer');
        const ingredientHtml = `
            <div class="ingredient mb-2">
                <input type="text" name="name" placeholder="材料名" required/>
                <input type="text" name="quantity" placeholder="数量" />
                <button type="button" class="btn btn-danger btn-sm remove-ingredient">削除</button>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', ingredientHtml);
        setupRemoveIngredientButtons();
    });
}

function setupRemoveIngredientButtons() {
    const removeButtons = document.querySelectorAll('.remove-ingredient');
    removeButtons.forEach(button => {
        button.addEventListener('click', function() {
            button.closest('.ingredient').remove();
        });
    });
}

function setupInstructionButton() {
    document.getElementById('addInstruction').addEventListener('click', function() {
        const container = document.getElementById('instructionsContainer');
        const instructionHtml = `
            <div class="instruction mb-2">
                <textarea name="content" placeholder="手順内容" required></textarea>
                <button type="button" class="btn btn-danger btn-sm remove-instruction">削除</button>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', instructionHtml);
        setupRemoveInstructionButtons();
    });
}

function setupRemoveInstructionButtons() {
    const removeButtons = document.querySelectorAll('.remove-instruction');
    removeButtons.forEach(button => {
        button.addEventListener('click', function() {
            button.closest('.instruction').remove();
        });
    });
}
