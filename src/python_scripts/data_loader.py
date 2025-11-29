import pandas as pd

class DataLoader:
    @staticmethod
    def load_from_xml(xml_path: str) -> pd.DataFrame:
        """Load dataset from an XML file (expects records under <record>)."""
        return pd.read_xml(xml_path, xpath='.//record')
    