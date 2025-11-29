import argparse
import pickle

from data_loader import DataLoader
from model_builder import ModelFactory
from preprocessor_builder import Preprocessor
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline


def train_model(
    dataset_path: str,
    output_path: str,
    algorithm: str = "RandomForest",
    n_estimators: int = 700,
    max_depth: int = None,
    test_size: float = 0.2,
    random_state: int = 42,
    hidden_layers: str = None,
    activation: str = "relu",
    max_iter: int = 200,
) -> int:
    df = DataLoader.load_from_xml(dataset_path)
    if "Salary" not in df.columns:
        raise ValueError("Target column 'Salary' not found in dataset")

    X = df.drop(columns=["Salary"])
    y = df["Salary"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=float(test_size), random_state=int(random_state)
    )

    preprocessor = Preprocessor.create(X_train)
    model = ModelFactory.create(
        algorithm,
        n_estimators=n_estimators,
        max_depth=max_depth,
        hidden_layers=hidden_layers,
        activation=activation,
        max_iter=max_iter,
        random_state=random_state,
    )

    pipeline = Pipeline([("preprocessor", preprocessor), ("model", model)])

    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    print(f"MAE={mae:.4f}")
    print(f"R2={r2:.4f}")

    with open(output_path, "wb") as f:
        pickle.dump(pipeline, f)

    return 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train ML model with preprocessing")
    parser.add_argument("--dataset", required=True, help="Path to dataset XML")
    parser.add_argument("--output", required=True, help="Output path for model")
    parser.add_argument(
        "--algorithm",
        default="LinearRegression",
        choices=[
            "LinearRegression",
            "RandomForest",
            "GradientBoosting",
            "NeuralNetwork",
        ],
    )
    parser.add_argument("--n_estimators", default=400, type=int)
    parser.add_argument("--max_depth", default=None)
    parser.add_argument("--test_size", default=0.2, type=float)
    parser.add_argument("--random_state", default=42, type=int)

    # Neural network args
    parser.add_argument(
        "--hidden_layers",
        default="100",
        help='Comma-separated sizes for hidden layers, e.g. "64,32"',
    )
    parser.add_argument(
        "--activation", default="relu", choices=["identity", "logistic", "tanh", "relu"]
    )
    parser.add_argument("--max_iter", default=200, type=int)

    args = parser.parse_args()

    try:
        exit_code = train_model(
            args.dataset,
            args.output,
            args.algorithm,
            args.n_estimators,
            args.max_depth,
            args.test_size,
            args.random_state,
            args.hidden_layers,
            args.activation,
            args.max_iter,
        )
        exit(exit_code)
    except Exception as e:
        print(f"ERROR: {e}")
        exit(1)
