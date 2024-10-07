CREATE TABLE recipes (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  image_path VARCHAR(255),
  recipe_source VARCHAR(255),
  servings VARCHAR(255),
  remark VARCHAR(255),
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY(id)
);

CREATE TABLE ingredients (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  quantity VARCHAR(255) ,
  unit VARCHAR(255),
  arrange BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY(id),
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE instructions (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  step_number INT NOT NULL,
  content TEXT NOT NULL,
  arrange BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY(id),
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);
