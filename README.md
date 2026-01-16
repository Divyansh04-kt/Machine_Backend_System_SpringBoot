# Machine Events Backend System

### Introduction-
This project is a backend system built using Java and Spring Boot to simulate how a factory backend receives and processes events from machines. Each machine when produce non-defective or defective items will send events to backend system.
The backend system stores these events, handles duplicates or updates, and provides statistics for a given time range.

### Architecture-
The application follows a 3 layered architecture:

Controller Layer → Service Layer → Store Layer (In-Memory)

1. Controller layer - Controller Layer will handle HTTP requests and responses.
   Example: /events/batch for ingestion and /stats for querying statistics.
2. Service layer - This layer contains the core business logic such as validation, deduplication, updates, and calculations.
3. Store layer - Store layer is responsible for storing events in memory(ConcurrentHashMap) using thread-safe data structures.

This separation makes the code easier to understand, test, and extend.



### Data Model-
The data model is simple and is carefully designed to support deduplication, updates, and fast queries.
Events are stored in an in-memory map: ConcurrentHashMap<String, Event> where:
Key → eventId
Value → latest valid Event information

Each Event contains:
1. eventId – Unique identifier for deduplication.
2. eventTime – The actual time when the event occurred (used for statistics queries).
3. receivedTime – The time when the system received the event (used for conflict resolution).
4. machineId and lineId – Metadata for grouping or future extensions.
5. durationMs – Processing or event duration.
6. defectCount – Number of defects; special values like -1 indicate “unknown”.

For statistics, the system does not maintain a separate data structure. Instead, it streams over the stored events and applies filters based on eventTime. This keeps the model simple and avoids synchronization issues between multiple stores.



### Deduplication and Update Logic-
Each event sent by the machine is uniquely identified by its eventId.
When a new event comes in, the system checks if an event with the same eventId already exists:
1. If no event exists → the event is accepted
2. If an event exists and the payload is exactly the same → event is deduped and it will be ignored.
3. If the payload is different, then receivedTime of the events will be checked:
   1) If the incoming event has a newer receivedTime, it updates the existing event in the records.
   2) If it has an older receivedTime, it is ignored.

Payload comparison is done using the equals() method of the Event class, which compares all important fields like event time, duration, machine ID, line ID, and defect count.



### Thread Safety-
The system is designed to be thread-safe because multiple machines can send events at the same time.

Thread Safety is achieved by:
1. Using ConcurrentHashMap for storage of the events.
2. Using the compute() method, which performs atomic read-modify-write operations per eventId.

Because of this, even if multiple threads ingest the same event concurrently, only one correct version is stored and no data corruption occurs.



### Stats and Query Logic-
Statistics are calculated using only eventTime to reflect when events actually occurred.
For machine stats, events are filtered by machineId and a time range where the start is inclusive and the end is exclusive. All valid events are counted, but events with defectCount = -1 are excluded from defect totals. The average defect rate is computed per hour and used to determine machine health.

For top defect lines, events are grouped by lineId within the given time window. Total defects, event counts, and defect percentages are calculated, then results are sorted by total defects and limited to the requested number of lines.



### Performance Strategy-
To handle at least 1000 events per second, the following strategies are used:
1. In-memory storage instead of database calls.
2. O(1) deduplication using hash maps.
3. Lock-free concurrency using ConcurrentHashMap.compute().
4. Minimal object creation during ingestion.

Statistics queries are read-only and operate on stable data, which keeps them fast even under concurrent writes.



### Edge Cases and Assumptions-
Edge Cases Handled-
1. Duplicate events (same eventId, same payload) - Identical events are deduplicated and stored only once.
2. Same eventId, different payload, newer receivedTime - Newer event overwrites the older stored event.
3. Same eventId, different payload, older receivedTime - Older event is ignored and does not overwrite newer data.
4. Out-of-order arrivals - Events may arrive in any order; receivedTime determines the winning record.
5. defectCount = -1 - Event is counted, but defect value is excluded from defect totals.
6. Invalid duration values - Events with invalid or negative duration are rejected.
7. Future eventTime - Events with eventTime in the future are rejected.
8. Time range boundaries - Start time is inclusive, end time is exclusive to avoid double counting.
9. Concurrent ingestion - Multiple threads ingesting the same event do not create duplicates.

Assumptions-
1. In-memory storage is sufficient - All events fit in memory for the scope of this assignment.
2. eventId uniquely identifies an event - All deduplication and updates are based on eventId.
3. receivedTime is trusted - Clock synchronization issues are handled outside the system.
4. Updates are less frequent than inserts - Most ingestions are new events, not updates.
5. Only latest event version is relevant - Historical versions are not retained.



### Setup and Run Instructions-
Prerequisites-
1. Java 17+
2. Maven
3. IntelliJ IDEA

Run the application - mvn spring-boot:run

Run tests - mvn test

Available APIs-
1. POST /events/batch
2. GET /stats
3. GET /stats/top-defect-lines



### Improvement-
In the future, the in-memory storage can be replaced with a database such as PostgreSQL or MySQL to make the system persistent and prevent data loss when the application restarts. Using a database would also allow the application to scale beyond a single instance, as multiple service instances could safely read and write to the same data store. With database support, transactional updates and constraints can enforce deduplication and update rules more reliably under high concurrency. Indexes on commonly queried fields like eventTime, lineId, and machineId would significantly improve query performance as the volume of events grows. Overall, adding a database would make the system more robust, scalable, and suitable for long-term production use.

