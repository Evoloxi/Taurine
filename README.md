# Taurine

Changes some of create's block entity renderers to use flyhwheel's instancing capabilities

Before[^1]:
<img width="2560" height="1440" alt="2026-05-26_18 15 28" src="https://github.com/user-attachments/assets/7d52312a-77a5-4797-a2e2-31c7a514f1ab" />
After:
<img width="2560" height="1440" alt="2026-05-26_18 15 55" src="https://github.com/user-attachments/assets/6e6bf7e4-51c6-44aa-80e6-bf855c91dcbc" />

Depots and filter slots[^2] are also included
<img width="2375" height="1438" alt="filter_slots" src="https://github.com/user-attachments/assets/b0ebebaa-3eb0-4716-bfc3-537bc791aa1e" />
<img width="2557" height="1438" alt="depots" src="https://github.com/user-attachments/assets/8f373f74-3a5c-45c8-877c-3386826bc146" />


 [^1]: Note that the belts themselves were also rendered via the slow cpu path, the fps with just the belt blocks on the fast path is around 150
 [^2]: Distance based unloading is currently not implemented
