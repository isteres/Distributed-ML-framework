from typing import List

import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


class Preprocessor:
    @staticmethod
    def create(X: pd.DataFrame) -> ColumnTransformer:
        """Create a ColumnTransformer for categorical and numeric features."""
        cat_cols: List[str] = X.select_dtypes(include=['object']).columns.tolist()
        num_cols: List[str] = X.select_dtypes(include=['int64', 'float64', 'number']).columns.tolist()

        cat_pipe = Pipeline([
            ('ohe', OneHotEncoder(handle_unknown='ignore', 
                                  sparse_output=True, 
                                  min_frequency=0.01))
        ])

        num_pipe = Pipeline([
            ('scaler', StandardScaler())
        ])

        preprocessor = ColumnTransformer([
            ('cat', cat_pipe, cat_cols),
            ('num', num_pipe, num_cols)
        ], remainder='drop')

        return preprocessor