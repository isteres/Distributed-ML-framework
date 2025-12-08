"""Utilities to build the preprocessing pipeline used before model training.

This module exposes a single `Preprocessor.create` factory that inspects a
training DataFrame and returns a `ColumnTransformer` combining one-hot
encoding for categorical fields and standard scaling for numeric features.
"""

from typing import List

import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


class Preprocessor:
    """Factory for building a preprocessing `ColumnTransformer` from a DataFrame.

    The class provides a single static `create` method used to derive which
    columns are categorical vs numeric and assemble the corresponding
    transformation pipelines.
    """

    @staticmethod
    def create(X: pd.DataFrame) -> ColumnTransformer:
        """Create a ColumnTransformer for categorical and numeric features.

        The returned transformer applies one-hot encoding to object-typed
        columns and standard scaling to numeric columns, dropping any
        remaining columns.
        """
        cat_cols: List[str] = X.select_dtypes(include=["object"]).columns.tolist()
        num_cols: List[str] = X.select_dtypes(
            include=["int64", "float64", "number"]
        ).columns.tolist()

        cat_pipe = Pipeline(
            [
                (
                    "ohe",
                    OneHotEncoder(
                        handle_unknown="ignore", sparse_output=True, min_frequency=0.01
                    ),
                )
            ]
        )

        num_pipe = Pipeline([("scaler", StandardScaler())])

        preprocessor = ColumnTransformer(
            [("cat", cat_pipe, cat_cols), ("num", num_pipe, num_cols)], remainder="drop"
        )

        return preprocessor
