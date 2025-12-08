# Distributed ML Framework — Distributed Systems  

This project, created for the Distributed Systems course, implements a
client-server platform that provides a set of practical exercises related to
supervised machine learning. The components demonstrate distributed
programming concepts through the use of concurrency and parallelism.

Short description of the repository contents (by package)

- `src/Framework/Client`
	- Client-side application and helpers. Contains the interactive console UI,
		the main client entry point, download logic for model files, and an
		inactivity watcher that disconnects idle clients.
	- Key files: `Client.java`, `ConsoleInterface.java`, `Main.java`,
		`ModelDownloaderThread.java`, `ChunkWriterThread.java`, `InactivityWatcher.java`.

- `src/Framework/Domain`
	- Plain data classes and domain objects used across client/server and
		network messages. Includes request objects for dataset insertion and
		inference, the `ModelChunk` structure used for file transfer, and enums.
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
		`predict.py` (prediction CLI that prints `PREDICTION:<value>`),
		`model_builder.py`, `preprocessor_builder.py`, `data_loader.py`.

Notes

- The codebase is intentionally structured to show distributed systems
	concepts: socket-based communication, concurrent request handling,
	coordination primitives, chunked file transfer, external process
	orchestration, and a minimal persistent metadata store.
- The repository contains source code and configuration; it does not include
	prebuilt binaries.

