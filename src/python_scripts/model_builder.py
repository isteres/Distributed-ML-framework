from typing import Optional, Tuple

from sklearn.ensemble import GradientBoostingRegressor, RandomForestRegressor
from sklearn.linear_model import LinearRegression
from sklearn.neural_network import MLPRegressor


def _parse_hidden_layers(s: Optional[str]) -> Tuple[int, ...]:
    if s is None or s == "None" or str(s).strip() == "":
        return (100, 50)
    parts = [p.strip() for p in str(s).split(",") if p.strip() != ""]
    return tuple(int(p) for p in parts)


class ModelFactory:
    @staticmethod
    def create(
        algorithm: str,
        n_estimators: int = 700,
        max_depth: Optional[int] = None,
        hidden_layers: Optional[str] = None,
        activation: str = "relu",
        max_iter: int = 200,
        random_state: int = 42,
    ):
        if algorithm == "RandomForest":
            return RandomForestRegressor(
                n_estimators=int(n_estimators),
                max_depth=int(max_depth)
                if max_depth and str(max_depth) != "None"
                else None,
                random_state=random_state,
            )
        if algorithm == "GradientBoosting":
            return GradientBoostingRegressor(
                n_estimators=int(n_estimators),
                max_depth=int(max_depth)
                if max_depth and str(max_depth) != "None"
                else 3,
                random_state=random_state,
            )
        if algorithm == "NeuralNetwork":
            hidden_tuple = _parse_hidden_layers(hidden_layers)
            return MLPRegressor(
                hidden_layer_sizes=hidden_tuple,
                activation=activation,
                max_iter=int(max_iter),
                random_state=random_state,
            )
        return LinearRegression()
