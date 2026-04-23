const { Octokit } = require("@octokit/rest");
const fs = require("fs");
const path = require("path");
const { glob } = require("glob");

async function sync() {
  const token = process.env.GITHUB_TOKEN;
  const repoFull = process.env.GITHUB_REPO;

  if (!token || !repoFull) {
    console.error("GITHUB_TOKEN or GITHUB_REPO not set");
    process.exit(1);
  }

  const [owner, repo] = repoFull.split("/");
  const octokit = new Octokit({ auth: token });

  console.log(`Syncing to ${owner}/${repo}...`);

  const rootDir = path.join(__dirname, "..", "..");
  
  const files = await glob("**/*", {
    cwd: rootDir,
    ignore: [
      "node_modules/**",
      "dist/**",
      ".git/**",
      "package-lock.json",
      "build/**",
      ".next/**",
      ".gradle/**",
    ],
    nodir: true,
  });

  // 1. Get default branch
  const { data: repoData } = await octokit.repos.get({ owner, repo });
  const branch = repoData.default_branch;
  console.log(`Using branch: ${branch}`);

  // 2. Get latest commit
  const { data: refData } = await octokit.git.getRef({
    owner,
    repo,
    ref: `heads/${branch}`,
  });
  const parentCommitSha = refData.object.sha;
  
  const { data: commitData } = await octokit.git.getCommit({
    owner,
    repo,
    commit_sha: parentCommitSha,
  });
  const baseTree = commitData.tree.sha;

  console.log(`Uploading ${files.length} files...`);

  // 3. Create blobs
  const blobs = await Promise.all(
    files.map(async (filePath) => {
      const fullPath = path.join(rootDir, filePath);
      const content = fs.readFileSync(fullPath, "utf8");
      const { data } = await octokit.git.createBlob({
        owner,
        repo,
        content,
        encoding: "utf-8",
      });
      return {
        path: filePath,
        mode: "100644",
        type: "blob",
        sha: data.sha,
      };
    })
  );

  // 4. Create tree
  const { data: newTree } = await octokit.git.createTree({
    owner,
    repo,
    base_tree: baseTree,
    tree: blobs,
  });

  // 5. Create commit
  const { data: newCommit } = await octokit.git.createCommit({
    owner,
    repo,
    message: `Auto-sync: ${new Date().toISOString()}`,
    tree: newTree.sha,
    parents: [parentCommitSha],
  });

  // 6. Update ref
  await octokit.git.updateRef({
    owner,
    repo,
    ref: `heads/${branch}`,
    sha: newCommit.sha,
  });

  console.log("Success! Files synced via API.");

  try {
    await octokit.repos.createCommitComment({
      owner,
      repo,
      commit_sha: newCommit.sha,
      body: "🚀 Sincronização automática concluída via AI Coding Agent!",
    });
  } catch (e) {
    console.log("Could not post comment, but sync was successful.");
  }
}

sync().catch((err) => {
  console.error("Critical Error:", err.message);
  if (err.request) console.error("Request details:", err.request);
  process.exit(1);
});
