const TASKS = 1000;
const WORKERS = 10_000;

const tasks = [];

for (let i = 1; i <= TASKS; i++) {
    const now = new Date();
    now.setSeconds(now.getSeconds() - Math.round((Math.random() * 10000)));
    const startedAt = now.toISOString();

    const workersCount = 1 + Math.round(Math.random() * 10);
    const workerIds = [];
    for (let j = 1; j <= workersCount; j++) {
        const workerId = `w-${1 + Math.round(Math.random() * (WORKERS - 1))}`;
        workerIds.push(workerId);
    }

    const task = {
        _id: `t-${i}`,
        title: `Task ${i}`,
        description: `This is the description for task ${i}.`,
        startedAt,
        completedAt: null,
        workerIds
    };
    tasks.push(task);
}

db.tasks.insertMany(tasks);

