I will implement the album creation and list functionality by connecting to the backend interfaces as requested.

### 1. Configuration

* **Modify** **`config.ts`**: Add `SPACE_ADD` (`/api/space/add`) and `SPACE_LIST_PAGE_VO` (`/api/space/list/page/vo`) to `API_ROUTES`.

### 2. Utilities & Types

* **Create** **`utils/date.ts`**: Add a helper function `formatDate` to format timestamps as `yyyy-mm-dd`.

* **Create** **`types/space.ts`**: Define interfaces for:

  * `Space` (Album data structure)

  * `SpaceLevel` (Enum: 0=Basic, 1=Pro, 2=Flagship)

  * `SpaceAddRequest` (Create album params)

  * `SpaceQueryRequest` (List album params)

  * `Page<T>` (Pagination wrapper)

### 3. Services

* **Create** **`services/space.ts`**: Implement the API calls:

  * `addSpace`: Sends a POST request to create an album.

  * `listSpaceVoByPage`: Sends a POST request to fetch the paginated album list.

  * ensure CSRF tokens are handled using `useAuthStore`.

### 4. Component Implementation

* **Update** **`components/Albums/AlbumsPage.tsx`**:

  * Replace mock data with real API calls using `useEffect`.

  * Implement `handleCreateAlbum` to call `addSpace` and refresh the list upon success.

  * **Layout & Design**:

    * Apply the responsive grid: `grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5`.

    * **Card Design**:

      * Use the fixed cover URL: `https://yuluo-picture-1383397986.cos.ap-guangzhou.myqcloud.com/Album_default_cover/default_album.webp`.

      * Implement hover effects (zoom + lift + shadow).

      * Display info: Name, Count, Level (with specific colors: Green/Blue/Gold), and formatted Create Time.

