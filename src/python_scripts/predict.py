"""Lightweight prediction CLI used by the server.

Loads a pickled preprocessing+model pipeline and prints a single-line
prediction to stdout using the format `PREDICTION:<value>`. On success the
script exits with code 0; on error it writes a message to stderr and exits
with code 1. This simple contract makes it easy for the Java server to parse
results and impose timeouts on the subprocess.
"""

import argparse
import pickle
import pandas as pd
import sys


def main():
    """Parse CLI args, load the model pipeline and print a single prediction.

    The function expects model input fields that match the XML dataset names
    used during training. The printed output is intentionally minimal and
    machine-parseable.
    """
    parser = argparse.ArgumentParser(description="Predict salary using trained model")
    parser.add_argument("--model", required=True, help="Path to trained model (.pkl)")
    parser.add_argument("--country", required=True, help="Country")
    parser.add_argument("--gender", required=True, help="Gender")
    parser.add_argument("--educational_level", required=True, help="Educational Level")
    parser.add_argument("--field_of_study", required=True, help="Field of Study")
    parser.add_argument(
        "--english_proficiency", required=True, help="English Proficiency"
    )
    parser.add_argument(
        "--internship_experience", required=True, help="Internship Experience"
    )
    parser.add_argument("--gpa", type=float, required=True, help="GPA")
    parser.add_argument("--age", type=int, required=True, help="Age")

    args = parser.parse_args()

    try:
        # Load model
        with open(args.model, "rb") as f:
            model = pickle.load(f)

        # Prepare input data (usar nombres del dataset XML)
        input_data = pd.DataFrame(
            [
                {
                    "Country_of_Origin": args.country,
                    "Gender": args.gender,
                    "Education_Level": args.educational_level,
                    "Field_of_Study": args.field_of_study,
                    "Language_Proficiency": args.english_proficiency,
                    "Internship_Experience": args.internship_experience,
                    "GPA_10": args.gpa,
                    "Age": args.age,
                }
            ]
        )

        # Make prediction
        prediction = model.predict(input_data)[0]

        # Print prediction (single line, for easy parsing)
        print(f"PREDICTION:{prediction}")
        sys.exit(0)

    except Exception as e:
        print(f"ERROR:{str(e)}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
