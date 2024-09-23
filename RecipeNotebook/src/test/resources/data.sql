INSERT INTO recipes (name, image_path, recipe_source, servings, remark, created_at, updated_at)
VALUES ('卵焼き', 'test1/path', 'https://------1.com', '2人分', '備考欄1', '2024-09-22 17:00:00', '2024-10-22 17:00:00');
INSERT INTO recipes (name, image_path, recipe_source, servings, remark, favorite, created_at, updated_at)
VALUES ('目玉焼き', 'test2/path', 'https://------2.com', '1人分', '備考欄2', TRUE, '2024-09-23 17:00:00', '2024-10-23 17:00:00');

INSERT INTO ingredients (recipe_id, name, quantity, unit, arrange)
VALUES
(1, '卵', 3, '個', FALSE), (1, 'サラダ油', NULL, NULL, FALSE), (1, '醤油', 0.5, '大さじ', FALSE), (1, '砂糖', 1, '大さじ', FALSE),
(2, '卵', 1, '個', FALSE), (2, 'サラダ油', NULL, NULL, FALSE), (2, '水', NULL, NULL, FALSE);

INSERT INTO instructions (recipe_id, step_number, instruction, arrange)
VALUES
(1, 1, '卵を溶いて調味料を混ぜ、卵液を作る', FALSE), (1, 2, 'フライパンに油をたらし、火にかける', FALSE),
(1, 3, '卵液を1/3くらいフライパンに入れて焼き、巻く', TRUE), (1, 4, '3の手順を繰り返して完成', FALSE),
(2, 1, 'フライパンに油をたらし、火にかける', FALSE), (2, 2, 'フライパンに卵を割り入れる', FALSE),
(2, 3, '少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成', FALSE);
