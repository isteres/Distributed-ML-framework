import argparse
import pandas as pd
import pickle
from typing import Optional
from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.metrics import r2_score, mean_absolute_error

def load_dataset_from_xml(xml_path: str) -> pd.DataFrame:
    """Load dataset from XML file using pandas"""
    df = pd.read_xml(xml_path, xpath='.//record')
    return df

def create_preprocessor(X: pd.DataFrame) -> ColumnTransformer:
    """Create preprocessing pipeline"""
    cat_cols = X.select_dtypes(include=['object']).columns
    num_cols = X.select_dtypes(include=['int64', 'float64']).columns
    
    # Pipeline para columnas categóricas
    cat_pipe = Pipeline([
        ('ohe', OneHotEncoder(handle_unknown='ignore',
                              sparse_output=True,
                              min_frequency=0.01))
    ])
    
    # Pipeline para columnas numéricas con normalización
    num_pipe = Pipeline([
        ('scaler', StandardScaler())
    ])
    
    # Combinar ambos pipelines
    preprocessor = ColumnTransformer([
        ('cat', cat_pipe, cat_cols),
        ('num', num_pipe, num_cols)
    ], remainder='drop')
    
    return preprocessor

def train_model(dataset_path: str, output_path: str, 
                algorithm: str = 'RandomForestRegressor',
                n_estimators: int = 100, max_depth: Optional[int] = None, 
                test_size: float = 0.2, random_state: int = 42) -> int:
    """Train ML model with preprocessing pipeline"""
    
    print(f"Loading dataset from {dataset_path}...")
    df = load_dataset_from_xml(dataset_path)
    
    print(f"Dataset shape: {df.shape}")
    
    # Separate features and target
    X = df.drop(columns=['Salary'])
    y = df['Salary']
    
    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=test_size, random_state=random_state
    )
    
    print(f"Training set: {X_train.shape}, Test set: {X_test.shape}")
    
    # Create preprocessor
    print("Creating preprocessing pipeline...")
    preprocessor = create_preprocessor(X_train)
    
    # Select algorithm
    print(f"Training with {algorithm}...")
    if algorithm == 'RandomForest':
        model = RandomForestRegressor(
            n_estimators=int(n_estimators),
            max_depth=int(max_depth) if max_depth and max_depth != 'None' else None,
            random_state=random_state
        )
    elif algorithm == 'GradientBoosting':
        model = GradientBoostingRegressor(
            n_estimators=int(n_estimators),
            max_depth=int(max_depth) if max_depth and max_depth != 'None' else 3,
            random_state=random_state
        )
    else:  # LinearRegression
        model = LinearRegression()
    
    # Create full pipeline (preprocessing + model)
    full_pipeline = Pipeline([
        ('preprocessor', preprocessor),
        ('model', model)
    ])
    
    # Train
    print("Training model...")
    full_pipeline.fit(X_train, y_train)
    
    # Evaluate
    print("Evaluating model...")
    y_pred = full_pipeline.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    
    # Print metrics (will be parsed by Java)
    print(f"MAE={mae:.4f}")
    print(f"R2={r2:.4f}")
    
    # Save full pipeline (preprocessing + model)
    print(f"Saving model to {output_path}...")
    with open(output_path, 'wb') as f:
        pickle.dump(full_pipeline, f)
    
    print(f"Model saved successfully!")
    return 0

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Train ML model with preprocessing')
    parser.add_argument('--dataset', required=True, help='Path to dataset XML')
    parser.add_argument('--output', required=True, help='Output path for model')
    parser.add_argument('--algorithm', default='LinearRegression', 
                        choices=['LinearRegression', 'RandomForest', 'GradientBoosting'],
                        help='Algorithm to use')
    parser.add_argument('--n_estimators', default='100', help='Number of estimators')
    parser.add_argument('--max_depth', default=None, help='Max depth')
    parser.add_argument('--test_size', default='0.2', help='Test size (0.0-1.0)')
    parser.add_argument('--random_state', default='42', help='Random state')
    
    args = parser.parse_args()
    
    try:
        exit_code = train_model(
            args.dataset,
            args.output,
            args.algorithm,
            args.n_estimators,
            args.max_depth,
            float(args.test_size),
            int(args.random_state)
        )
        exit(exit_code)
    except Exception as e:
        print(f"ERROR: {str(e)}")
        exit(1)