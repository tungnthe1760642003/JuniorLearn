# CI/CD level 1 cho project nay

Muc tieu cua bo cau hinh nay la cho mot ban junior nhin vao la hieu duoc:

1. CI lam gi khi co `push` hoac `pull request`
2. CD lam gi khi code da vao `main`
3. Artifact va Docker image duoc tao ra o dau

## 1. CI: `.github/workflows/ci.yml`

CI chay o cac nhanh:

- `main`
- `develop`
- `feature/**`
- moi `pull_request`

Luong CI hien tai:

1. `checkout` source code
2. cai Java 17
3. chay `mvn clean verify`

Neu test fail hoac build fail thi pull request phai duoc sua truoc khi merge.

Lenh chinh:

```bash
mvn -B -ntp -s .mvn/settings.xml clean verify
```

Y nghia:

- `-B`: chay che do batch cho CI
- `-ntp`: bot log download cho gon
- `-s .mvn/settings.xml`: ep Maven dung local repository trong project

## 2. Vi sao can `.mvn/settings.xml`

May dev va may CI co the co cau hinh Maven khac nhau. File nay giup thong nhat cho luu dependency ve `./.m2repo`, tranh phu thuoc may ca nhan.

Dieu nay hop voi level moi di lam:

- build de lap lai
- de debug khi pipeline fail
- khong bi mo ho vi config global tren may

## 3. CD: `.github/workflows/cd.yml`

CD chay khi:

- `push` vao `main`
- bam chay tay bang `workflow_dispatch`

Luong CD hien tai:

1. build project bang Maven
2. upload file `.jar` thanh GitHub Actions artifact
3. copy `.jar` thanh `app.jar`
4. build Docker image
5. push image len `ghcr.io`

Day la muc CD co ban nhung thuc te:

- ban co artifact de tai ve
- ban co image de deploy tiep len VPS, Render, Railway, EC2 hoac Kubernetes

## 4. Dockerfile

`Dockerfile` dang o muc toi gian:

- dung `eclipse-temurin:17-jre`
- copy `app.jar`
- expose cong `8082`
- chay `java -jar app.jar`

Muon build local tren PowerShell:

```powershell
mvn -s .mvn/settings.xml clean package
Copy-Item (Get-ChildItem target\*.jar | Select-Object -First 1) app.jar
docker build -t junior-spring-interview:local .
docker run -p 8082:8082 junior-spring-interview:local
```

## 5. Muon GitHub Actions chay that thi can gi

Project hien tai phai duoc day len GitHub repository. Toi thieu:

```bash
git init
git add .
git commit -m "Add basic CI/CD"
git branch -M main
git remote add origin <your-github-repo>
git push -u origin main
```

Sau do vao tab `Actions` tren GitHub de xem pipeline chay.

## 6. Hoc gi tu bo cau hinh nay

Neu dang o tam 1 nam kinh nghiem, nen nam chac cac y sau:

- CI khac CD o muc dich gi
- tai sao phai co build, test truoc khi merge
- artifact khac Docker image the nao
- tai sao pipeline nen chay giong nhau giua local va CI
- tai sao branch `main` thuong la noi kich hoat CD

## 7. Buoc nang cap tiep theo

Khi hieu xong level 1, co the nang len:

1. them `branch protection` de bat buoc CI pass moi duoc merge
2. them job scan dependency
3. them deploy sang mot moi truong that
4. them `staging` va `production`
5. them approval truoc khi deploy production
