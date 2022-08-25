<template>
  <div>
    <div class="search-bar">
      <div class="searchbar-input-container">
        <el-icon :size="50"><Search /></el-icon>
        <div style="margin: 0 1%"></div>
        <el-input
          v-model="input"
          placeholder="搜索，不同关键字用;(英文分号)隔开"
          size="large"
          maxlength="300"
        />
        <div style="margin: 0 1%"></div>
        <div style="display: flex; align-items: center">
          <el-button type="primary" @click="search">搜索</el-button>
        </div>
      </div>
    </div>
    <div class="search-case">
      <div class="search-case-container">
        <div>过滤条件：</div>
        <div>
          <el-checkbox-group v-model="searchCaseList">
            <el-checkbox-button label="f">
              <span>仅搜索文件</span>
            </el-checkbox-button>
            <el-checkbox-button label="d">
              <span>仅搜索文件夹</span>
            </el-checkbox-button>
            <el-checkbox-button label="full">
              <span>全字匹配</span>
            </el-checkbox-button>
            <el-checkbox-button label="case">
              <span>关键字不忽略大小写</span>
            </el-checkbox-button>
          </el-checkbox-group>
        </div>
      </div>
    </div>
    <div v-if="searchResultsList.length !== 0" class="results">
      <div
        v-for="each in searchResultsList"
        :key="each.filePath"
        class="each-result"
      >
        <div>
          <div class="file-name">{{ each.fileName }}</div>
          <div class="file-path">{{ each.filePath }}</div>
        </div>
        <div>
          <el-button
            type="success"
            @click="downloadFile(each.fileName, each.filePath)"
            >下载</el-button
          >
        </div>
      </div>
    </div>
    <div v-else class="results">
      <div
        class="each-result"
        style="text-align: center; display: flex; justify-content: center"
      >
        {{ tips }}
      </div>
    </div>
    <div class="page">
      <div class="page-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-count="totalPages"
          v-model:current-page="pageNum"
          :hide-on-single-page="true"
        />
      </div>
    </div>
  </div>
</template>

<script>
import searchApi from "@/api/searchApi.js";
import { useLoading } from "vue-loading-overlay";

export default {
  data() {
    return {
      input: "",
      searchCaseList: [],
      searchResultsList: [],
      pageNum: 1,
      pageSize: 10,
      totalPages: 1,
      tips: "还没有任何结果",
    };
  },
  watch: {
    pageNum(_val) {
      this.getResults();
    },
  },
  methods: {
    /**
     * 下载文件
     */
    downloadFile(fileName, filePath) {
      const fileFullPath = filePath + "\\" + fileName;
      searchApi
        .download(fileFullPath)
        .then((res) => {
          const fileBlob = new Blob([res]);
          fileBlob.text().then((text) => {
            //判断是否无后缀名，可能为文件夹压缩包
            if (fileName.indexOf(".") === -1) {
              const fileHeader = String.fromCharCode(0x50, 0x4b, 0x03, 0x04);
              // 判断是否为zip格式
              if (text.startsWith(fileHeader)) {
                fileName += ".zip";
              }
            }
            const url = window.URL.createObjectURL(fileBlob);
            const link = document.createElement("a");
            link.style.display = "none";
            link.href = url;
            link.setAttribute("download", fileName); // 自定义下载文件名
            document.body.appendChild(link);
            link.click();
          });
        })
        .catch((err) => {
          console.error(err);
        });
    },
    /**
     * 通过用户输入和选择过滤条件生成搜索字符串
     */
    generateSearchStr() {
      if (this.searchCaseList.length !== 0) {
        const searchCaseStr = this.searchCaseList.join(";");
        return this.input + ":" + searchCaseStr;
      } else {
        return this.input;
      }
    },
    getFileName(fileFullPath) {
      if (fileFullPath) {
        const index = fileFullPath.lastIndexOf("\\");
        return fileFullPath.substring(index + 1);
      }
      return "";
    },
    getParentPath(fileFullPath) {
      if (fileFullPath) {
        const index = fileFullPath.lastIndexOf("\\");
        return fileFullPath.substring(0, index);
      }
      return "";
    },
    /**
     * 获取第pageNum页的数据
     */
    getResults() {
      const self = this;
      this.searchResultsList = [];
      searchApi
        .getResults(this.pageNum, this.pageSize)
        .then((res) => {
          res.data.forEach((eachFilePath) => {
            this.searchResultsList.push({
              fileName: self.getFileName(eachFilePath),
              filePath: self.getParentPath(eachFilePath),
            });
          });
          self.totalPages = res.pages;
        })
        .catch((err2) => {
          console.error(err2);
          self.tips = "获取数据失败";
        });
    },
    search() {
      this.tips = "";
      this.pageNum = 1;
      this.totalPages = 1;
      const str = this.generateSearchStr();
      const $loading = useLoading();
      // 发起请求
      const loader = $loading.show();
      searchApi
        .search(str)
        .then(() => {
          this.getResults();
          loader.hide();
        })
        .catch((err) => {
          console.error(err);
          self.tips = "发送搜索请求失败";
          loader.hide();
        });
    },
  },
};
</script>

<style scoped>
.search-bar {
  width: 100%;
  height: 20%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.searchbar-input-container {
  width: 50%;
  display: flex;
  justify-content: flex-start;
}
.search-case {
  width: 100%;
  height: 20%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1%;
}
.search-case-container {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.results {
  width: 100%;
  display: block;
  margin-top: 2%;
}

.each-result {
  width: 55%;
  text-align: left;
  margin: 1% auto;
  display: flex;
  justify-content: space-between;
}
.file-name {
  font-size: 1rem;
}
.file-path {
  color: gray;
}
.page {
  width: 100%;
  display: flex;
  justify-content: center;
}
</style>
