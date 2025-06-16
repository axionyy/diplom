from datetime import datetime

from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session, sessionmaker
from pydantic import BaseModel, ValidationError
from typing import List, Optional
from database import *
import logging

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Создание таблиц в базе данных
Base.metadata.create_all(bind=engine)

app = FastAPI()
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


# Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


class UserLogin(BaseModel):
    login: str
    password: str


class UserRegister(BaseModel):
    name: str
    surname: str
    height: float
    weight: float
    gender: bool
    birthday: str
    password: str
    login: str
    photo: str = None  # Добавлено поле для фотографии пользователя


class UserUpdate(BaseModel):
    login: Optional[str] = None
    name: Optional[str] = None
    surname: Optional[str] = None
    height: Optional[float] = None
    birthday: Optional[str] = None
    password: Optional[str] = None


class VerifyPasswordRequest(BaseModel):
    user_id: int
    password: str


class WeightRecordCreate(BaseModel):
    date: str
    weight: float
    notes: Optional[str] = None


class WeightRecordResponse(BaseModel):
    id: int
    date: str
    weight: float
    bmi: Optional[float]
    notes: Optional[str]


class WeightRecordCreate(BaseModel):
    date: str
    weight: float


class RecipeCreateRequest(BaseModel):
    name: str
    callories: float
    photo: Optional[str] = None
    components: str
    steps: str
    squirrels: float = 0
    fats: float = 0
    carbohydrates: float = 0


class RecipeUpdateRequest(BaseModel):
    name: str
    callories: float
    photo: Optional[str] = None
    components: str
    steps: str
    squirrels: float
    fats: float
    carbohydrates: float


@app.post("/login")
def login(user_login: UserLogin, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.login == user_login.login).first()
    if not user or user.password != user_login.password:
        raise HTTPException(status_code=400, detail="Invalid login or password")

    return {
        "id": user.id,
        "name": user.name,
        "login": user.login,
        "birthday": user.birthday.strftime("%Y-%m-%d") if user.birthday else None,
        "message": "Login successful"
    }


@app.post("/register")
def register(user_register: UserRegister, db: Session = Depends(get_db)):
    try:
        # Проверка, существует ли уже пользователь с таким логином
        existing_user = db.query(User).filter(User.login == user_register.login).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="Login already exists")

        # Создание нового пользователя
        new_user = User(
            name=user_register.name,
            surname=user_register.surname,
            height=user_register.height,
            weight=user_register.weight,
            gender=user_register.gender,
            birthday=user_register.birthday,
            password=user_register.password,
            login=user_register.login,
            photo=user_register.photo  # Добавлено поле для фотографии пользователя
        )

        # Добавление нового пользователя в базу данных
        db.add(new_user)
        db.commit()
        db.refresh(new_user)

        return {"message": "Registration successful", "user_id": new_user.id}
    except ValidationError as e:
        logger.error(f"Validation error: {e.json()}")
        raise HTTPException(status_code=422, detail=e.json())
    except Exception as e:
        logger.error(f"Registration failed: {e}")
        raise HTTPException(status_code=500, detail="Registration failed")


@app.get("/users/{user_id}")
def get_user(user_id: int, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return {
        "id": user.id,
        "name": user.name,
        "login": user.login,
        "birthday": user.birthday.strftime("%Y-%m-%d") if user.birthday else None,
        "height": user.height,  # Добавлено поле роста
        "surname": user.surname,  # Также добавьте другие нужные поля
        "weight": user.weight,
        "gender": user.gender
    }


@app.put("/users/{user_id}")
def update_user(user_id: int, user_update: UserUpdate, db: Session = Depends(get_db)):
    try:
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # Обновляем только переданные поля
        if user_update.login is not None:
            user.login = user_update.login
        if user_update.name is not None:
            user.name = user_update.name
        if user_update.surname is not None:
            user.surname = user_update.surname
        if user_update.height is not None:
            user.height = user_update.height
        if user_update.birthday is not None:
            user.birthday = user_update.birthday
        if user_update.password is not None:
            user.password = user_update.password  # В реальности нужно хэшировать

        db.commit()

        return {
            "id": user.id,
            "login": user.login,
            "name": user.name,
            "surname": user.surname,
            "height": user.height,
            "birthday": user.birthday
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/users/verify-password")
def verify_password(verify_request: VerifyPasswordRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == verify_request.user_id).first()
    if not user:
        return {"is_valid": False}

    # В реальном приложении используйте хэширование:
    # if not verify_password_hash(verify_request.password, user.password):
    if user.password != verify_request.password:
        return {"is_valid": False}

    return {"is_valid": True}


@app.post("/users/{user_id}/weight-records")
def create_weight_record(
        user_id: int,
        record: WeightRecordCreate,
        db: Session = Depends(get_db)
):
    try:
        # Проверяем существование пользователя
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # Рассчитываем BMI (опционально)
        bmi = None
        if user.height:
            bmi = record.weight / ((user.height / 100) ** 2)

        # Создаем новую запись
        new_record = UserWeightHistory(
            user_id=user_id,
            date=record.date,
            weight=record.weight,
            bmi=bmi,
            notes=record.notes
        )

        db.add(new_record)
        db.commit()
        db.refresh(new_record)

        return {
            "id": new_record.id,
            "date": new_record.date.strftime("%Y-%m-%d"),
            "weight": new_record.weight,
            "bmi": new_record.bmi,
            "notes": new_record.notes
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/users/{user_id}/weight-records")
def get_weight_records(
        user_id: int,
        limit: Optional[int] = 100,
        db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    records = db.query(UserWeightHistory) \
        .filter(UserWeightHistory.user_id == user_id) \
        .order_by(UserWeightHistory.date.desc()) \
        .limit(limit) \
        .all()

    return [{
        "id": r.id,
        "date": r.date.strftime("%Y-%m-%d"),
        "weight": r.weight,
        "bmi": r.bmi,
        "notes": r.notes
    } for r in records]


@app.delete("/weight-records/{record_id}")
def delete_weight_record(
        record_id: int,
        db: Session = Depends(get_db)
):
    record = db.query(UserWeightHistory).filter(UserWeightHistory.id == record_id).first()
    if not record:
        raise HTTPException(status_code=404, detail="Record not found")

    try:
        db.delete(record)
        db.commit()
        return {"message": "Weight record deleted successfully"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/users/{user_id}/weight-history")
def create_weight_record(
        user_id: int,
        record: WeightRecordCreate,
        db: Session = Depends(get_db)
):
    try:
        # Проверяем существование пользователя
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # Создаем новую запись в истории
        new_record = UserWeightHistory(
            user_id=user_id,
            date=record.date,
            weight=record.weight
        )

        db.add(new_record)

        # Обновляем текущий вес пользователя
        user.weight = record.weight

        db.commit()

        return {
            "id": new_record.id,
            "date": new_record.date.strftime("%Y-%m-%d"),
            "weight": new_record.weight
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/users/{user_id}/weight-history")
def get_weight_history(
        user_id: int,
        db: Session = Depends(get_db)
):
    records = db.query(UserWeightHistory) \
        .filter(UserWeightHistory.user_id == user_id) \
        .order_by(UserWeightHistory.date.desc()) \
        .all()

    return [{
        "id": r.id,
        "date": r.date.strftime("%Y-%m-%d"),
        "weight": r.weight
    } for r in records]


@app.post("/users/{user_id}/recipes")
def create_recipe(
    user_id: int,
    recipe: RecipeCreateRequest,
    db: Session = Depends(get_db)
):
    try:
        # Проверяем существование пользователя
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # Создаем новый рецепт
        new_recipe = Reciep(
            name=recipe.name,
            callories=recipe.callories if recipe.callories else 0,
            photo=recipe.photo,
            components=recipe.components,
            steps=recipe.steps,
            squirrels=recipe.squirrels if recipe.squirrels else 0,
            fats=recipe.fats if recipe.fats else 0,
            carbohydrates=recipe.carbohydrates if recipe.carbohydrates else 0,
            userID=user_id,
            dateCreate=datetime.now().date()
        )

        db.add(new_recipe)
        db.commit()
        db.refresh(new_recipe)

        return {"message": "Рецепт успешно создан", "id": new_recipe.id}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=f"Ошибка создания рецепта: {str(e)}")


@app.get("/users/{user_id}/recipes")
def get_user_recipes(user_id: int, db: Session = Depends(get_db)):
    recipes = db.query(Reciep)\
        .filter(Reciep.userID == user_id)\
        .order_by(Reciep.dateCreate.desc())\
        .all()

    return [{
        "id": r.id,
        "name": r.name,
        "photo": r.photo,
        "callories": r.callories,
        "components": r.components,
        "steps": r.steps,
        "squirrels": r.squirrels,
        "fats": r.fats,
        "carbohydrates": r.carbohydrates,
        "dateCreate": r.dateCreate.strftime("%Y-%m-%d")
    } for r in recipes]


@app.put("/recipes/{recipe_id}")
def update_recipe(
    recipe_id: int,
    recipe: RecipeUpdateRequest,
    db: Session = Depends(get_db)
):
    try:
        db_recipe = db.query(Reciep).filter(Reciep.id == recipe_id).first()
        if not db_recipe:
            raise HTTPException(status_code=404, detail="Рецепт не найден")

        db_recipe.name = recipe.name
        db_recipe.callories = recipe.callories
        db_recipe.photo = recipe.photo
        db_recipe.components = recipe.components
        db_recipe.steps = recipe.steps
        db_recipe.squirrels = recipe.squirrels
        db_recipe.fats = recipe.fats
        db_recipe.carbohydrates = recipe.carbohydrates

        db.commit()
        return {"message": "Рецепт успешно обновлен"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.delete("/recipes/{recipe_id}")
def delete_recipe(recipe_id: int, db: Session = Depends(get_db)):
    try:
        db_recipe = db.query(Reciep).filter(Reciep.id == recipe_id).first()
        if not db_recipe:
            raise HTTPException(status_code=404, detail="Рецепт не найден")

        db.delete(db_recipe)
        db.commit()
        return {"message": "Рецепт успешно удален"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(e))