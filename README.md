# Distributed ML Framework — Distributed Systems  

This project, created for the Distributed Systems course, implements a
client-server platform that provides a set of practical exercises related to
supervised machine learning. The components demonstrate distributed
programming concepts through the use of concurrency and parallelism.

Short description of the repository contents 

- `Datasets/`
  	- Contains all the datasets availables to work with(i.e. `dataset1000.xml`). The dataset name
  	  specifies the amount of records it has. Their structure is specified by `dataset.dtd`.

- `src/Framework/Client`
	- Client-side application and helpers. Contains the interactive console UI,
		the main client entry point, download logic for model files, and an
		inactivity watcher that disconnects idle clients.
	- Key files: `Client.java`, `ConsoleInterface.java`, `Main.java`,
		`ModelDownloaderThread.java`, `ChunkWriterThread.java`, `InactivityWatcher.java`.

- `src/Framework/Domain`
	- Plain data classes and domain objects used across client/server and
		network messages. 
	- Key files: `DatasetInsertRequest.java`, `InferenceRequest.java`,
		`TrainingRequest.java`, `ModelChunk.java`, `WorkerWithStudies.java`,
		`Enums/StudentEnums.java`.

- `src/Framework/Persistence`
	- Lightweight persistence layer backed by an XML file. Implements a
		singleton `ServerDatabase` that reads/writes metadata (users, trained
		models, metrics) using the DOM API and a small DTD.
	- Key files: `ServerDatabase.java`, plus the XML/DTD resources used for
		storage.

- `src/Framework/Server`
	- Server-side logic: TCP acceptor, per-connection handler, background
		tasks and threads that perform model training, prediction orchestration,
		and model transfer. Demonstrates thread pools, synchronization
		primitives (barriers/latches), scheduled tasks and process orchestration.
	- Key files: `Server.java`, `ConnectionHandler.java`, `ModelTrainerThread.java`,
		`InferenceThread.java`, `ModelSenderThread.java`, `DatasetInserterThread.java`,
		`DailyTrainingTask.java`.

- `src/python_scripts`
	- Python helper scripts used by the server to perform the actual ML work.
		The Java server launches these scripts as subprocesses and parses their
		output for metrics and prediction values.
	- Key files: `main.py` (training CLI that builds the training pipeline),
		`predict.py` (prediction CLI), `model_builder.py`, `preprocessor_builder.py`,
		 `data_loader.py`. In `requirements.txt` you can find all the dependencies needed
   		to execute the scripts. 

## Future improvements
- Change the routes to the python scripts, so the server can be launched from Linux and Windows
environment(now it has Windows routes).
- Split the server workload in different nodes depending on the amount of users connected. So
the server doesn't get overloaded. 


