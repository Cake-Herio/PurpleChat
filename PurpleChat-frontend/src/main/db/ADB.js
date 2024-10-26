const fs = require('fs');
const fse = require('fs-extra');
const os = require('os');
const util = require('util');
const path = require('path');
const NODE_ENV = process.env.NODE_ENV;
const userDir = os.homedir();
const dbFolder = path.join(userDir, NODE_ENV === "development" ? ".PurpleChatDBTest" : path.join(".PurpleChat", "DB"));
if (!fs.existsSync(dbFolder)) {
  fse.ensureDirSync(dbFolder);
}

import {
  add_tables,
  add_index,
  alter_tables
} from './Tables';

let db;
let dbRun;
let dbGet;
let dbAll;

const initializeSQLite = () => {
  const sqlite3 = require('sqlite3').verbose();
  db = new sqlite3.Database(dbFolder + "/PurpleChat.db");
  dbRun = util.promisify(db.run.bind(db));
  dbGet = util.promisify(db.get.bind(db));
  dbAll = util.promisify(db.all.bind(db));
};

// 全局变量，存储表的字段(下划线） 和 json对象（驼峰） 的映射关系
const globalColumnsMap = {};

// 服务初始化时自动创建表 
const createTable = async () => {
  try {
    for (const item of add_tables) {
      await dbRun(item);
    }
    // 创建索引
    for (const item of add_index) {
      await dbRun(item);
    }
    // 修改表
    for (const item of alter_tables) {
      const fieldList = await queryAll(`PRAGMA table_info(${item.tableName})`, []);
      const field = fieldList.some(row => row.name === item.field);
      if (!field) {
        await dbRun(item.sql);
      }
    }
  } catch (err) {
    console.error("Error creating tables:", err);
    throw err;
  }
};

// 查询操作
const queryAll = async (sql, params) => {
  try {
    const rows = await dbAll(sql, params);
    return rows.map(item => convertDbObj2Json(item));
  } catch (err) {
    console.error("Error querying database:", err);
    throw err;
  }
};

// 返回数据库的记录数
const queryCount = async (sql, params) => {
  try {
    const row = await dbGet(sql, params);
    return row.count;
  } catch (err) {
    console.error("Error querying database:", err);
    throw err;
  }
}

// 查单个
const queryOne = async (sql, params) => {
  try {
    const row = await dbGet(sql, params);
    return convertDbObj2Json(row);
  } catch (err) {
    console.error("Error querying database:", err);
    throw err;
  }
}

const run = async (sql, params) => {
  try {
    console.log("sql:", sql, "params:", JSON.stringify(params))
    await dbRun(sql, params);
    return this.changes; //返回执行的条数
  } catch (err) {
    console.error("Error running sql:", err);
    throw err;
  }
}

// sqlPrefix是sql语句的前缀，包括insert等
const OPInsertData = async (sqlPrefix, tableName, data) => {
  const columnsMap = globalColumnsMap[tableName];
  if (!columnsMap) {
    console.error(`Table name ${tableName} does not exist in globalColumnsMap`);
    throw new Error(`Invalid table name: ${tableName}`);
  }

  const dbColumns = [];
  const params = [];
  // 根据参数
  for (let key in data) {
    if (data[key] != undefined && columnsMap[key] != undefined) {
      dbColumns.push(columnsMap[key]);
      params.push(data[key]);
    }
  }
  if (dbColumns.length === 0 || params.length === 0) {
    console.error('No valid columns or parameters for insertion');
    throw new Error('Invalid data for insertion');
  }

  const preparedValues = Array(dbColumns.length).fill("?").join(",");
  const sql = `${sqlPrefix} ${tableName} (${dbColumns.join(",")}) VALUES (${preparedValues})`; 
  return await run(sql, params);
}

const OPUpdate = async (tableName, data, paramData) => {
  const columnsMap = globalColumnsMap[tableName];

  if (!columnsMap) {
    console.error(`Table name ${tableName} does not exist in globalColumnsMap`);
    throw new Error(`Invalid table name: ${tableName}`);
  }

  const dbColumns = [];
  const params = [];
  const whereColumns = []; // 条件
  for (let key in data) {
    if (data[key] != undefined && columnsMap[key] != undefined) {
      dbColumns.push(`${columnsMap[key]} = ?`);
      params.push(data[key]);
    }
  }
  for (let key in paramData) {

    if (paramData[key] != undefined && columnsMap[key] != undefined) {
      params.push(paramData[key]);
      whereColumns.push(`${columnsMap[key]} = ?`);
    }
  }
  if (dbColumns.length === 0 || params.length === 0 || whereColumns.length === 0) {
    console.error('No valid columns or parameters for insertion');
    throw new Error('Invalid data for insertion');
  }

  const sql = `update ${tableName} set ${dbColumns.join(",")} where ${whereColumns.join(" and ")}`;
  return await run(sql, params);
}

const insert = async (tableName, data) => {
  return await OPInsertData('insert into', tableName, data);
}

const insertOrReplace = async (tableName, data) => {
  return await OPInsertData('insert or replace into', tableName, data);
}

const insertOrIgnore = async (tableName, data) => {
  return await OPInsertData('insert or ignore into', tableName, data);
}


const update = async (tableName, data, paramData) => {
  return await OPUpdate(tableName, data, paramData);
}

//------------------------------------------------------------------------------------

const convertDbObj2Json = (dbObj) => {
  if (dbObj == null) {
    return null;
  }
  const bizData = {};
  for (let item in dbObj) {
    bizData[convertKey2Camel(item)] = dbObj[item];
  }
  return bizData;
};

// 将 key 名称转换为驼峰命名
const convertKey2Camel = (key) => {
  const arr = key.split("_");
  let ret = "";
  let i = 0;
  for (const item of arr) {
    if (i == 0) {
      ret += item.charAt(0) + item.slice(1);
      i = 1;
      continue;
    }
    ret += item.charAt(0).toUpperCase() + item.slice(1);
  }
  return ret;
};

// 建立数据库字段和对象的映射关系
// 获取表的所有字段
const initTableColumnsMap = async () => {
  try {
    let sql = `select * from sqlite_master where type='table' AND name!='sqlite_sequence'`;
    let tables = await queryAll(sql, []);
    for (let i = 0; i < tables.length; i++) {
      let tableColumns = await queryAll(`PRAGMA table_info(${tables[i].name})`, []);
      const columnMapItem = {};
      for (let j = 0; j < tableColumns.length; j++) {
        columnMapItem[convertKey2Camel(tableColumns[j].name)] = tableColumns[j].name;
      }
      globalColumnsMap[tables[i].name] = columnMapItem;
    }
  } catch (err) {
    console.error("Error initializing table columns map:", err);
    throw err;
  }
};

const init = async () => {
  try {
    initializeSQLite(); // 延迟加载 SQLite
    await createTable();
    await initTableColumnsMap();
  } catch (err) {
    console.error("Error during initialization:", err);
  }
};


export {
  createTable,
  run,
  queryAll,
  queryOne,
  queryCount,
  insertOrReplace,
  insertOrIgnore,
  insert,
  update,
  init,
};
