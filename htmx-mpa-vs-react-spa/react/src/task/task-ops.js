import { api } from "../shared/api";

class TasksOps {
  constructor(api) {
    this._api = api;
  }

  getAllowedProjects() {
    return this._api.get("/projects/names");
  }
}

export const tasksOps = new TasksOps(api);